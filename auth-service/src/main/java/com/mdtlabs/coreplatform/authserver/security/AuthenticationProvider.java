package com.mdtlabs.coreplatform.authserver.security;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mdtlabs.coreplatform.common.util.DateUtil;
import com.mdtlabs.coreplatform.common.util.StringUtil;
import com.mdtlabs.coreplatform.authservice.repository.RoleRepository;
import com.mdtlabs.coreplatform.authservice.repository.UserRepository;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.CustomDateSerializer;
import com.mdtlabs.coreplatform.common.logger.Logger;
import com.mdtlabs.coreplatform.common.model.dto.TimezoneDTO;
import com.mdtlabs.coreplatform.common.model.entity.Role;
import com.mdtlabs.coreplatform.common.model.entity.User;

/**
 * <p>
 * Extend spring {@code AuthenticationProvider} and check authentication
 * precondition like isActiveRole, isEnabled..etc.
 * </p>
 * 
 * @author Vigneshkumar created on 30 Jun 2022
 *
 */
public class AuthenticationProvider implements org.springframework.security.authentication.AuthenticationProvider {

	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Value("${app.login-time-limit-in-hour}")
	private int loginTimeLimitInHour;

	@Value("${app.login-count-limit}")
	private int loginCountLimit;

	/**
	 * <p>
	 * Set current password in to spring {@code PasswordEncoder} to check is
	 * matches.
	 * </p>
	 * 
	 * @param passwordEncoder - Set encrypted password.
	 */
	@Autowired
	@Qualifier(Constants.PASSWORDENCODER)
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * <p>
	 * Extended spring security authentication to check the following precondition.
	 * </p>
	 * <p>
	 * 1. Check the given username and password is blank.
	 * </p>
	 * <p>
	 * 2. Check the employee exist.
	 * </p>
	 * <p>
	 * 3. Check the employee is enabled.
	 * </p>
	 * <p>
	 * 4. If password matches then check the active role assign to the employee.
	 * </p>
	 * 
	 * @return a fully authenticated object including credentials. May return
	 *         <code>null</code> if the <code>AuthenticationProvider</code> is
	 *         unable to support authentication of the passed
	 *         <code>Authentication</code> object. In such a case, the next
	 *         <code>AuthenticationProvider</code> that supports the presented
	 *         <code>Authentication</code> class will be tried.
	 * 
	 * @throws AuthenticationException if authentication fails.
	 */
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username = String.valueOf(authentication.getPrincipal()).toLowerCase();
		String password = String.valueOf(authentication.getCredentials());
		User user = authenticationCheck(username, password);
		Set<Role> roles = user.getRoles();
		Map<String, Role> rolesAsMap = getRoleAsMap(roles);
		boolean isActiveRole = Boolean.FALSE;
		List<Role> rolesList = roleRepository.getAllRoles(Boolean.TRUE);
		for (Role role : rolesList) {
			if (null != rolesAsMap.get(role.getName())) {
				isActiveRole = Boolean.TRUE;
			}
		}
		if (!isActiveRole) {
			throw new BadCredentialsException(Constants.ERROR_INVALID_USER);
		}
		boolean isAuthenticated = Boolean.FALSE;
		if (password.equals(user.getPassword())) {
			isAuthenticated = Boolean.TRUE;
		}
		if (null != user.getTimezoneId()) {
			ModelMapper modelMapper = new ModelMapper();
			CustomDateSerializer.USER_ZONE_ID = modelMapper.map(user.getTimezoneId(), TimezoneDTO.class).getOffset();
		}
		if (isAuthenticated) {
			Set<GrantedAuthority> authorityList = user.getAuthorities();
			user.setRoles(roles);
			return new UsernamePasswordAuthenticationToken(user, password, authorityList);
		}
		Logger.logError(StringUtil.constructString(Constants.INFO_USER_PASSWORD_NOT_MATCH, username));
		throw new BadCredentialsException(Constants.ERROR_INVALID_USER);
	}

	/**
	 * This method is used to check whether the user account is valid
	 * 
	 * @param username - user name of the user
	 * @param password - password of the user
	 * @return User - user entity
	 */
	private User authenticationCheck(String username, String password) {
		if ((isBlank(username)) || (isBlank(password))) {
			throw new BadCredentialsException(Constants.ERROR_USERNAME_PASSWORD_BLANK);
		}
		User user = userRepository.getUserByUsername(username, Boolean.TRUE);
		if (null == user) {
			Logger.logError(StringUtil.constructString(Constants.INFO_USER_NOT_EXIST, username));
			throw new BadCredentialsException(Constants.ERROR_INVALID_USER);
		}

		if (Boolean.TRUE.equals(user.getIsBlocked())) {
			Logger.logError(StringUtil.constructString(Constants.ERROR_INVALID_ATTEMPTS));
			throw new BadCredentialsException(Constants.ERROR_INVALID_ATTEMPTS);
		}
		if ((!user.getPassword().equals(password) && isLoginLimitExceed(user)) || !user.isEnabled()) {
			Logger.logError(StringUtil.constructString(Constants.ERROR_ACCOUNT_DISABLED));
			throw new BadCredentialsException(Constants.ERROR_ACCOUNT_DISABLED);
		}

		Logger.logInfo(StringUtil.constructString(Constants.INFO_USER_EXIST, String.valueOf(user.isEnabled())));
		return user;
	}

	/**
	 * <p>
	 * Convert the role collection in to map. Map key as role name and value as
	 * object.
	 * </p>
	 * 
	 * @param roles - Role collection to map.
	 * @return - Map with role name as key and role object as value.
	 */
	private Map<String, Role> getRoleAsMap(Set<Role> roles) {
		Map<String, Role> roleAsMap = new HashMap<>();
		roles.forEach(role -> roleAsMap.put(role.getName(), role));
		return roleAsMap;
	}

	/**
	 * <p>
	 * Returns <code>true</code> if this <Code>AuthenticationProvider</code>
	 * supports the indicated <Code>Authentication</code> object.
	 * </p>
	 * 
	 * @param authentication - the class type of authentication is being passed
	 *
	 * @return <code>true</code> if the implementation can more closely evaluate the
	 *         <code>Authentication</code> class presented
	 */
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	/**
	 * <p>
	 * check whether the sequence is blank or not
	 * </p>
	 * 
	 * @param charSequence - Check the charactersequence is blank.
	 * @return true / false based on string isBlank.
	 */
	private boolean isBlank(CharSequence charSequence) {
		int stringLength;
		if ((charSequence == null) || ((stringLength = charSequence.length()) == Constants.ZERO)) {
			return Boolean.TRUE;
		}
		for (int character = Constants.ZERO; character < stringLength; character++) {
			if (!Character.isWhitespace(charSequence.charAt(character))) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	/**
	 * This method is used to check whether the login limit is exceeded
	 * 
	 * @param username - username of the user is passed
	 * @return - true or false will be returned based on login limit exceed
	 */
	public boolean isLoginLimitExceed(User user) {
		if (null == user) {
			return Boolean.FALSE;
		}
		int invalidLoginAttempts = user.getInvalidLoginAttempts();
		Date invalidLoginTime = DateUtil.formatDate(user.getInvalidLoginTime());
		Date currentDate = DateUtil.formatDate(new Date());
		long differenceInHours = DateUtil.getDiffInHours(currentDate, invalidLoginTime);
		if (differenceInHours >= loginTimeLimitInHour) {
			user.setInvalidLoginTime(currentDate);
			setUserValues(user, Constants.ONE, Boolean.FALSE, Boolean.TRUE);
		} else {
			if (invalidLoginAttempts >= loginCountLimit) {
				user.setBlockedDate(currentDate);
				setUserValues(user, Constants.ZERO, Boolean.TRUE, Boolean.FALSE);
				userRepository.save(user);
				return Boolean.TRUE;
			} else if (invalidLoginAttempts >= Constants.ZERO) {
				user.setInvalidLoginAttempts(++invalidLoginAttempts);
			}
		}
		userRepository.save(user);
		return Boolean.FALSE;
	}

	/**
	 * This method is used to set user entity values
	 * 
	 * @param user                 - user entity
	 * @param invalidLoginAttempts - invalid login attempts
	 * @param isBlocked            - user blocked or not
	 * @param isActive             - user active or not
	 */
	private void setUserValues(User user, int invalidLoginAttempts, boolean isBlocked, boolean isActive) {
		user.setInvalidLoginAttempts(invalidLoginAttempts);
		user.setIsBlocked(isBlocked);
		user.setActive(isActive);
	}

}
