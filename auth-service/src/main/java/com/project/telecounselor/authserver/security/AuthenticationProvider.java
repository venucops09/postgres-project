package com.project.telecounselor.authserver.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.project.telecounselor.common.util.CustomDateSerializer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.project.telecounselor.authservice.repository.RoleRepository;
import com.project.telecounselor.authservice.repository.UserRepository;
import com.project.telecounselor.common.Constants;
import com.project.telecounselor.common.logger.TelecounselorLogger;
import com.project.telecounselor.common.util.CommonUtil;
import com.project.telecounselor.model.entity.Role;
import com.project.telecounselor.model.entity.User;

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

	public static final String ERROR_USERNAME_PASSWORD_BLANK = "No Username and / or Password Provided";
	public static final String ERROR_INVALID_PASSWORD = "Invalid password";
	public static final String ERROR_INVALID_USER = "Invalid credentials";
	public static final String ERROR_ACCOUNT_DISABLED = "Disabled Account";
	public static final String ERROR_INVALID_ATTEMPTS = "Account locked due to multiple invalid login attempts.";
	public static final String ERROR_INVALID_USERNAME_PASSOWRD = "Username or Password is Invalid";

	public static final String INFO_EMPLOYEE = "Login requested user ";
	public static final String INFO_EMPLOYEE_NOT_EXIST = "Username does not exist : ";
	public static final String INFO_EMPLOYEE_EXIST = "Login employee isEnabled : ";
	public static final String INFO_EMPLOYEE_PASSWORD_NOT_MATCH = "Password doesn't match for the user : ";
	public static final String INFO_ROLE_NOT_EXIST = "Role doesn't exist for the user : ";

	public static final String uniqName = "unique_name";
	public static final String PREFERRED_USERNAME = "preferred_username";
	public static final String OSTYPE = "osType";
	public static final String MOBILE = "mobile";
	public static final String BAD_TOKEN = "Bad Token";
	public static final String INVALID_TOKEN = "Invalid Token";
	public static final String DOT_SPLITTER = "\\.";
	public static final String BRACE_FORMAT = "},}";

	private PasswordEncoder passwordEncoder;

	private RestTemplate restService = new RestTemplate();

	private ModelMapper modelMapper = new ModelMapper();

	@Autowired
	private org.springframework.cloud.client.discovery.DiscoveryClient discoveryClient;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;

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

		if ((isBlank(username)) || (isBlank(password))) {
			throw new BadCredentialsException(ERROR_USERNAME_PASSWORD_BLANK);
		}
		String userIp = getUserInfo() + "/user-service/user";

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, Constants.LOGIN_USER_AUTH);
		User user = userRepository.getUserByUsername(username, true);
		if (null == user) {
			TelecounselorLogger.logError(CommonUtil.constructString(INFO_EMPLOYEE_NOT_EXIST, username));
			throw new BadCredentialsException(ERROR_INVALID_USER);
		}
		if (null != user.getTimezone()) {
			CustomDateSerializer.USER_ZONE_ID = user.getTimezone().getTimezoneOffset();
		}

		if (user.getIsBlocked()) {
			TelecounselorLogger.logError(CommonUtil.constructString(ERROR_INVALID_ATTEMPTS));
			throw new BadCredentialsException(ERROR_INVALID_ATTEMPTS);
		}
		if (!user.getPassword().equals(password)) {
			HttpHeaders header = new HttpHeaders();
			header.add(HttpHeaders.AUTHORIZATION, Constants.LOGIN_USER_AUTH);
			HttpEntity<User> userEntity = new HttpEntity<>(header);
			ResponseEntity<Map> isLoginLimitExceed = restService.exchange(userIp + "/isLoginLimitExceed/" + username, HttpMethod.GET, userEntity,
					Map.class);
			if ((boolean)isLoginLimitExceed.getBody().get("entity")) {
				TelecounselorLogger.logError(CommonUtil.constructString(isLoginLimitExceed.getBody().get("message").toString()));
				throw new BadCredentialsException(isLoginLimitExceed.getBody().get("message").toString());
			}
		}

		TelecounselorLogger.logInfo(CommonUtil.constructString(INFO_EMPLOYEE_EXIST, String.valueOf(user.isEnabled())));
		if (!user.isEnabled()) {
			throw new BadCredentialsException(ERROR_ACCOUNT_DISABLED);
		}
		Set<Role> roles = user.getRoles();
		Map<String, Role> rolesAsMap = getRoleAsMap(roles);
		boolean isActiveRole = false;
		HttpHeaders header = new HttpHeaders();
		header.add(HttpHeaders.AUTHORIZATION, Constants.LOGIN_USER_AUTH);
		List<Role> rolesList = roleRepository.getAllRoles(true);
		for (Role role : rolesList) {
			if (null != rolesAsMap.get(role.getName())) {
				isActiveRole = true;
			}
		}
		if (!isActiveRole) {
			throw new BadCredentialsException(ERROR_INVALID_USER);
		}
		boolean isAuthenticated = false;
		if (password.equals(user.getPassword())) {
			isAuthenticated = true;
		}
		if (isAuthenticated) {
			Set<GrantedAuthority> authorityList = user.getAuthorities();
			user.setRoles(roles);
			return new UsernamePasswordAuthenticationToken(user, password, authorityList);
		}
		TelecounselorLogger.logError(CommonUtil.constructString(INFO_EMPLOYEE_PASSWORD_NOT_MATCH, username));
		throw new BadCredentialsException(ERROR_INVALID_USER);
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
		Map<String, Role> roleAsMap = new HashMap<String, Role>();
		roles.forEach(role -> {
			roleAsMap.put(role.getName(), role);
		});
		return roleAsMap;
	}

	/**
	 * <p>
	 * Returns <code>true</code> if this <Code>AuthenticationProvider</code>
	 * supports the indicated <Code>Authentication</code> object.
	 * </p>
	 * 
	 * @param authentication -
	 *
	 * @return <code>true</code> if the implementation can more closely evaluate the
	 *         <code>Authentication</code> class presented
	 */
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	/**
	 * <p>
	 * This method is used to get the user.
	 * </p>
	 * service URL from service discovery.
	 * 
	 * @return String
	 */
	private String getUserInfo() {
		String ipInfo = "";
		ServiceInstance instance = null;
		try {
			List<ServiceInstance> instanceList = new ArrayList<>();
			instanceList = discoveryClient.getInstances("USER");
			if (!instanceList.isEmpty()) {
				instance = instanceList.get(0);
			}
			if (null != instance) {
				ipInfo = instance.getUri().toString();
			}
		} catch (Exception e) {
			e.getMessage();
		}
		return ipInfo;
	}

	/**
	 * <p>
	 * Check charsequence is blank.
	 * </p>
	 * 
	 * @param cs - Check the charactersequence is blank.
	 * @return true / false based on string isBlank.
	 */
	private boolean isBlank(CharSequence cs) {
		int strLen;
		if ((cs == null) || ((strLen = cs.length()) == 0)) {
			return Boolean.TRUE;
		}
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

}
