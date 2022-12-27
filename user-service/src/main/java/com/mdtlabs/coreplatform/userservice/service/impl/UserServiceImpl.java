package com.mdtlabs.coreplatform.userservice.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.spec.SecretKeySpec;
import javax.transaction.Transactional;
import javax.xml.bind.DatatypeConverter;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.userservice.repository.UserRepository;
import com.mdtlabs.coreplatform.userservice.service.OrganizationService;
import com.mdtlabs.coreplatform.userservice.service.RoleService;
import com.mdtlabs.coreplatform.userservice.service.UserService;
import com.mdtlabs.coreplatform.AuthenticationFilter;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.ErrorConstants;
import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.TableConstants;
import com.mdtlabs.coreplatform.common.contexts.UserContextHolder;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataConflictException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.exception.Validation;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import com.mdtlabs.coreplatform.common.logger.Logger;
import com.mdtlabs.coreplatform.common.model.dto.EmailDTO;
import com.mdtlabs.coreplatform.common.model.dto.UserDTO;
import com.mdtlabs.coreplatform.common.model.entity.EmailTemplate;
import com.mdtlabs.coreplatform.common.model.entity.EmailTemplateValue;
import com.mdtlabs.coreplatform.common.model.entity.Notification;
import com.mdtlabs.coreplatform.common.model.entity.Organization;
import com.mdtlabs.coreplatform.common.model.entity.Role;
import com.mdtlabs.coreplatform.common.model.entity.User;
import com.mdtlabs.coreplatform.common.model.entity.UserToken;
import com.mdtlabs.coreplatform.common.model.entity.spice.OutBoundEmail;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;
import com.mdtlabs.coreplatform.common.repository.UserTokenRepository;
import com.mdtlabs.coreplatform.common.util.CommonUtil;
import com.mdtlabs.coreplatform.common.util.DateUtil;
import com.mdtlabs.coreplatform.common.util.StringUtil;

import org.springframework.web.client.RestTemplate;

/**
 * <p>
 * This service class contain all the business logic for user module and perform
 * all the user operation here.
 * </p>
 * 
 * @author Rajkumar created on Jun 30, 2022
 */
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private UserTokenRepository userTokenRepository;

	@Autowired
	private GenericRepository<UserToken> genericRepository;

	@Autowired
	private AuthenticationFilter authenticationFilter;

	@Autowired
	private RoleService roleService;
	
	@Autowired
	private OrganizationService organizationService;

	@Value("${app.page-count}")
	private int gridDisplayValue;

	@Value("${app.login-time-limit-in-hour}")
	private int loginTimeLimitInHour;

	@Value("${app.login-count-limit}")
	private int loginCountLimit;

	@Value("${app.forget-password-count-limit}")
	private int forgotPasswordCountLimit;

	@Value("${app.mail-user}")
	private String mailUser;

	@Value("${app.reset-password-count-limit}")
	private int resetPasswordCountLimit;

	@Value("${app.email-app-url}")
	private String appUrl;
	
	@Value("${app.forgot-password-time-limit-in-minutes}")
	private int forgotPasswordtimeLimitInMinutes;
	
	@Value("${app.reset-password-time-limit-in-minutes}")
	private int resetPasswordtimeLimitInMinutes;
	
	private ModelMapper modelMapper = new ModelMapper();
	
	private RestTemplate restService = new RestTemplate();

	/**
	 * {@inheritDoc}
	 */
	public User addUser(User user) {
		if (null != user.getRoles()) {
			if (null != getUserByUsername(user.getUsername())) {
				throw new Validation(1009);
			}
			Set<Role> roles = roleService.getRolesByIds(user.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
			user.setRoles(roles);
			user.setForgetPasswordCount(Constants.ZERO);
			if (null != user.getOrganizations()) {
				Set<Organization> organizations = organizationService.getOrganizationsByIds(
						user.getOrganizations().stream().map(Organization::getId).collect(Collectors.toList()));
				user.setOrganizations(organizations);
				user.setTenantId(organizations.stream().findFirst().get().getId());
			}
			User newUser = userRepository.save(user);
			if (Objects.nonNull(newUser)) {
				forgotPassword(user.getUsername(), Boolean.TRUE);
			}
			return newUser;
		}
		throw new Validation(1007);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getUsers(int pageNumber) {
		Pageable pageable = PageRequest.of(pageNumber - Constants.ONE, gridDisplayValue);
		Page<User> users = userRepository.getUsers(Boolean.TRUE, pageable);
		if (Objects.nonNull(users)) {
			return users.stream().collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	/**
	 * {@inheritDoc}
	 */
	public User updateUser(User user) {
		User exisitingUser = getUserById(user.getId());
		if (null == exisitingUser) {
			throw new Validation(1010);
		}
		if (exisitingUser.getUsername().equals(user.getUsername())) {
			if (null != user.getRoles()) {
				Set<Role> roles = roleService.getRolesByIds(user.getRoles().stream().map(Role::getId).collect(Collectors.toList()));
				user.setRoles(roles);
				if (null != user.getOrganizations()) {
					Set<Organization> organizations = organizationService.getOrganizationsByIds(
							user.getOrganizations().stream().map(Organization::getId).collect(Collectors.toList()));
					
					boolean isExist = organizations.stream().anyMatch(org -> (org.getId() == user.getTenantId()));
					user.setTenantId(isExist ? user.getTenantId() : organizations.stream().findFirst().get().getId());
					user.setOrganizations(organizations);
				}
				return userRepository.save(user);
			}
			throw new Validation(1105);
		}
		throw new Validation(1104);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteUserById(long userId) {
		UserDTO userDto = UserContextHolder.getUserDto();
		User deletedUser = null;
		if (null != userDto.getRoles()) {
			User user = getUserById(userId);
			if (null != user) {
				user.setActive(Boolean.FALSE);
				user.setDeleted(Boolean.TRUE);
				user.setUpdatedBy(userDto.getId());
				deletedUser = userRepository.save(user);
				return Boolean.TRUE;
			}
			return (deletedUser == null) ? Boolean.FALSE : Boolean.TRUE;
		}
		throw new Validation(1011);
	}

	/**
	 * {@inheritDoc}
	 */
	public User getUserById(long userId) {
		return userRepository.getUserById(userId, Boolean.TRUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public User getUserByUsername(String username) {
		return userRepository.getUserByUsername(username, Boolean.TRUE);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<User> getUsersByTenantIds(List<Long> tenantIds) {
		return userRepository.findUsersByTenantIds(tenantIds);
	}
	
	/**
	 * This method is used for validating user list.
	 * 
	 * @param parentOrganizationId parent organization id
	 * @param requestUsers         List of users to validate
	 * @return List<user> - List of user entities
	 */
	public List<User> validateUser(Long parentOrganizationId, List<User> requestUsers) {
		if (Objects.isNull(requestUsers) || 0 == requestUsers.size()) {
			throw new DataNotAcceptableException(10000);
		}

		List<User> validatedUsers = new ArrayList<>();
		List<Long> existingUsersIds = new ArrayList<>();
		List<String> newUserEmails = new ArrayList<>();

		for (User user : requestUsers) {
			if (Objects.isNull(user.getId()) || 0 == user.getId()) {
				newUserEmails.add(user.getUsername());
			} else {
				existingUsersIds.add(user.getId());
			}
		}

		if (!Objects.isNull(parentOrganizationId) && !existingUsersIds.isEmpty()) {
			validatedUsers = userRepository.findByIsActiveTrueAndIdIn(existingUsersIds);
			if (validatedUsers.size() != existingUsersIds.size()) {
				throw new DataNotFoundException(1102);
			}
		} else if (Objects.isNull(parentOrganizationId) && !existingUsersIds.isEmpty()) {
			throw new DataConflictException(1103);
		}

		if (!Objects.isNull(newUserEmails) && !newUserEmails.isEmpty()) {
			validatedUsers = userRepository.findByUsernameIn(newUserEmails);
			if (!Objects.isNull(validatedUsers) && !validatedUsers.isEmpty()) {
				throw new DataConflictException(1103);
			}
			validatedUsers = null;
		}
		return validatedUsers;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> addOrganizationUsers(List<User> users, List<String> roles, boolean isSiteUser) {
		if (Objects.isNull(users) || users.isEmpty()) {
			throw new BadRequestException(10000);
		}
//		if (isSiteUser) {
//			users.forEach(user -> user.setForgetPasswordCount(Constants.ZERO));
//		} else {
//			Set<Role> userRoles = roleService.getRolesByName(roles);
//			if (userRoles.isEmpty()) {
//				throw new DataNotFoundException(2002);
//			}
//			users.forEach(user -> {
//				user.setRoles(userRoles);
//				user.setForgetPasswordCount(Constants.ZERO);
//			});
//		}
//		users = userRepository.saveAll(users);
//		if (!Objects.isNull(users) && !users.isEmpty()) {
//			users.forEach(user -> forgotPassword(user.getUsername(), Boolean.TRUE));
//		}
		for (User user : users) {
			addUser(user);
		}
		return users;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean updatePassword(String token, String password) {
		Key secretKeySpec = secretKeySpecCreation();
		Claims body = Jwts.parser().setSigningKey(secretKeySpec).parseClaimsJws(token).getBody();
		String userName = (String) body.get(FieldConstants.USERNAME);
		User user = userRepository.getUserByUsername(userName, Boolean.TRUE);
		String oldPassword = user.getPassword();
		if (null == user || !token.equals(user.getForgetPasswordToken())) {
			Logger.logError(StringUtil.constructString(ErrorConstants.LINK_EXPIRED));
			throw new Validation(3009);
		}
		if (null != oldPassword && oldPassword.equals(password)) {
			Logger.logError(StringUtil.constructString(ErrorConstants.SAME_PASSWORD));
			throw new Validation(1012);
		}
		try {
			checkUsernameSameAsPassword(user.getUsername(), password);
		} catch (NoSuchAlgorithmException e) {
			Logger.logError( StringUtil.concatString(ErrorConstants.ERROR_UPDATE_PASWORD, e.getMessage()));
		}
		user.setPassword(password);
		user.setForgetPasswordToken(null);
		user.setForgetPasswordCount(0);
		return null != userRepository.save(user);
	}
	
	/**
	 * This method is used to compare username and password
	 * 
	 * @param username - user name of user
	 * @param password - password of user
	 * @throws NoSuchAlgorithmException
	 */
	private void checkUsernameSameAsPassword(String username, String password) throws NoSuchAlgorithmException {
		String salt = Constants.SALT_KEY;
		MessageDigest md = MessageDigest.getInstance(Constants.HASHING_CODE);
		md.update(salt.getBytes(StandardCharsets.UTF_8));
		byte[] bytes = md.digest(username.getBytes(StandardCharsets.UTF_8));
		StringBuilder sb = new StringBuilder();
		for (int hash = Constants.ZERO; hash < bytes.length; hash++) {
			sb.append(Integer.toString((bytes[hash] & 0xff) + 0x100, 16).substring(Constants.ONE));
		}
		if (password.equals(sb.toString())) {
			Logger.logError(StringUtil.constructString(ErrorConstants.PASSWORD_ERROR));
			throw new Validation(1014);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean forgotPassword(String emailId, boolean isFromCreation) {
		User user = userRepository.getUserByUsername(emailId, true);
		if (null != user) {
			String forgotPasswordToken = user.getForgetPasswordToken();
			int existingForgotPasswordCount = user.getForgetPasswordCount();

			if (Boolean.TRUE.equals(user.getIsBlocked())) {
				Logger.logError(Constants.ERROR_USER_BLOCKED);
				throw new Validation(1101);
			}
			boolean isForgotPasswordLimitExceed = checkForgotPasswordLimitExceed(user, isFromCreation);
			if (isForgotPasswordLimitExceed) {
				Logger.logError(Constants.ERROR_USER_BLOCKED);
				throw new Validation(1101);
			}

			String jwtToken = null;
			try {
				jwtToken = forgotPasswordTokenCreation(user);
				sendEmail(user, jwtToken, isFromCreation);
				user.setForgetPasswordToken(jwtToken);
				userRepository.save(user);
				userTokenRepository.deleteByUserId(user.getId());
				return Boolean.TRUE;
			} catch (Exception exception) {
				Logger.logError(String.valueOf(exception));
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * <p>
	 * Generate forgot password token.
	 * </p>
	 * @param user
	 * @return
	 */
	private String forgotPasswordTokenCreation(User user) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Constants.AES_KEY_TOKEN);
		Key secretKeySpec = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put(FieldConstants.USERNAME, user.getUsername());
		JwtBuilder jwtBuilder = Jwts.builder().setClaims(userInfo).signWith(signatureAlgorithm, secretKeySpec);
		String jwtToken = jwtBuilder.setId(String.valueOf(user.getId()))
				.setExpiration(Date.from(ZonedDateTime.now().plusHours(Constants.TWENTY_FOUR).toInstant()))
				.setIssuedAt(Date.from(ZonedDateTime.now().toInstant())).setIssuer(Constants.ISSUER).compact();
		return jwtToken;
	}

	/**
	 * {@inheritDoc}
	 */
	public UserToken createUserToken(long id, Map<String, String> userInfo) {
		UserToken userToken = new UserToken();
		userToken.setUserId(id);
		userToken.setAuthToken(userInfo.get(Constants.JWE_TOKEN));
		userToken.setRefreshToken(userInfo.get(Constants.JWE_REFRESH_TOKEN));
		return genericRepository.save(userToken);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getTotalSize() {
		return userRepository.getUsers(Boolean.TRUE).size();
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendEmail(User user, String jweToken, boolean isFromCreation) {
		EmailTemplate emailTemplate = new EmailTemplate();
		String notificationIp = getNotificationInfo() + "/email";
		Map<String, String> data = new HashMap<>();
		try {
			EmailDTO emailDto = new EmailDTO();
			if (isFromCreation) {
				HttpEntity<String> entity = CommonUtil.getCurrentEntity();
				ResponseEntity<Map> emailTemplateResponse = restService.exchange(
						notificationIp + "/email-type/" + Constants.NEW_USER_CREATION, HttpMethod.GET, entity,
						Map.class);
				if (emailTemplateResponse.getBody() == null
						|| emailTemplateResponse.getBody().get(FieldConstants.ENTITY) == null) {
					throw new Validation(3010);
				} else {
					emailDto = constructUserCreationEmail(user, jweToken, data, emailDto, emailTemplateResponse);
					createOutBoundEmail(notificationIp, emailDto);
				}
			} else {
				ResponseEntity<Map> emailTemplateResponse = restService
						.getForEntity(notificationIp + "/email-type/" + Constants.FORGOT_PASSWORD_USER, Map.class);
				if (emailTemplateResponse.getBody() == null
						|| emailTemplateResponse.getBody().get(FieldConstants.ENTITY) == null) {
					throw new Validation(3010);
				} else {
					emailDto = constructForgotEmail(user, jweToken, data, emailDto, emailTemplateResponse);
					createOutBoundEmail(notificationIp, emailDto);
				}
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}
	
	/**
	 * This method is used to construct the email
	 * 
	 * @param user                  - user entity
	 * @param jwtToken              - jwt token
	 * @param data                  - content of email
	 * @param emailDto              - email entity
	 * @param emailTemplateResponse - email template response
	 */
	private EmailDTO constructUserCreationEmail(User user, String jwtToken, Map<String, String> data, EmailDTO emailDto,
			ResponseEntity<Map> emailTemplateResponse) {
		EmailTemplate emailTemplate = modelMapper.map(emailTemplateResponse.getBody().get(FieldConstants.ENTITY),
				EmailTemplate.class);
		for (EmailTemplateValue emailTemplateValue : emailTemplate.getEmailTemplateValues()) {
			if (Constants.APP_URL_EMAIL.equalsIgnoreCase(emailTemplateValue.getName())) {
				data.put(Constants.APP_URL_EMAIL, appUrl + jwtToken);
				emailTemplateValue.setValue(appUrl + jwtToken);
			}
		}
		emailDto.setBody(StringUtil.parseEmailTemplate(emailTemplate.getBody(), data));
		emailDto.setEmailTemplate(emailTemplate);
		emailDto.setSubject(Constants.RESET_NOTIFICATION_SUBJECT);
		emailDto.setTo(user.getUsername());
		emailDto.setFormDataId(user.getId());
		emailDto.setFormName(Constants.EMAIL_FORM_USER);
		return emailDto;
	}

	/**
	 * This method is used to construct mail for forget password trigger
	 * 
	 * @param user                  - user entity
	 * @param jweToken              - jwe token
	 * @param data                  - mail content
	 * @param emailDto              - email dto
	 * @param emailTemplateResponse - email template response
	 */
	private EmailDTO constructForgotEmail(User user, String jwtToken, Map<String, String> data, EmailDTO emailDto,
			ResponseEntity<Map> emailTemplateResponse) {
		EmailTemplate emailTemplate = modelMapper.map(emailTemplateResponse.getBody().get(FieldConstants.ENTITY),
				EmailTemplate.class);
		for (EmailTemplateValue emailTemplateValue : emailTemplate.getEmailTemplateValues()) {
			if (Constants.APP_URL_EMAIL.equals(emailTemplateValue.getName())) {
				emailTemplateValue.setValue(appUrl + jwtToken);
				data.put(Constants.APP_URL_EMAIL, appUrl + jwtToken);
			} else if (FieldConstants.FORGET_PASSWORD_TOKEN.equals(emailTemplateValue.getName())) {
				emailTemplateValue.setValue(jwtToken);
				data.put(FieldConstants.FORGET_PASSWORD_TOKEN, jwtToken);
			}
		}
		emailDto.setBody(StringUtil.parseEmailTemplate(emailTemplate.getBody(), data));
		emailDto.setFromName(mailUser);
		emailDto.setSubject(Constants.FORGOT_NOTIFICATION_SUBJECT);
		emailDto.setTo(user.getUsername());
		emailDto.setFrom(mailUser);
		emailDto.setFormDataId(user.getId());
		emailDto.setFormName(Constants.EMAIL_FORM_USER);
		return emailDto;
	}


	/**
	 * This method is used to send mail in runnable thread
	 * 
	 * @param notificationIp - notification info
	 * @param emailDto-      email entity
	 */
	private void createOutBoundEmail(String notificationIp, EmailDTO emailDto) {
		OutBoundEmail outBoundEmail = modelMapper.map(emailDto, OutBoundEmail.class);
		HttpHeaders header = new HttpHeaders();
		header.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<OutBoundEmail> emailEntity = new HttpEntity<>(outBoundEmail, header);
		ResponseEntity<Map> emailResponse = restService.exchange(notificationIp + "/create", HttpMethod.POST,
				emailEntity, Map.class);
		boolean isSuccess = modelMapper.map(emailResponse.getBody().get(FieldConstants.ENTITY), Boolean.class);
		if (!isSuccess) {
			throw new Validation(3011);
		} 
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean loginLimitExceed(String username) {
		User user = userRepository.getUserByUsername(username, Boolean.TRUE);
		if (null == user) {
			return Boolean.FALSE;
		}
		int invalidLoginAttempts = user.getInvalidLoginAttempts();
		Date invalidLoginTime = DateUtil.formatDate(user.getInvalidLoginTime());
		Date currentDate = DateUtil.formatDate(new Date());
		long differenceInHours = DateUtil.getDiffInHours(currentDate, invalidLoginTime);
		if (differenceInHours >= loginTimeLimitInHour) {
			user.setInvalidLoginTime(currentDate);
			setBlockInformation(user, Constants.ONE, Boolean.FALSE);
		} else {
			if (invalidLoginAttempts < loginCountLimit && invalidLoginAttempts >= Constants.ZERO) {
				user.setInvalidLoginAttempts(++invalidLoginAttempts);
			}
			if (invalidLoginAttempts >= loginCountLimit) {
				user.setBlockedDate(currentDate);
				setBlockInformation(user, Constants.ZERO, Boolean.TRUE);
			}
		}
		userRepository.save(user);
		return Boolean.TRUE;
	}

	/**
	 * This method is used to set block information of user
	 * 
	 * @param user                - user entity
	 * @param invalidLoginAttempt - invalid login attempt value
	 * @param flag                - user blocked or no as true or falsea
	 */
	private void setBlockInformation(User user, int invalidLoginAttempt, Boolean flag) {
		user.setInvalidLoginAttempts(invalidLoginAttempt);
		user.setIsBlocked(flag);
	}

	/**
	 * {@inheritDoc}
	 */
	/**
	 * {@inheritDoc}
	 */
	public boolean isResetPasswordLimitExceed(String username) {
	   User user = userRepository.getUserByUsername(username, true);
	   if (null == user) {
	      return false;
	   }
	   int passwordResetAttempts = user.getPasswordResetAttempts();
	   Date invalidResetTime = DateUtil.formatDate(user.getInvalidResetTime());
	   Date currentDate = DateUtil.formatDate(new Date());
	   long getDateDiffInMinutes = DateUtil.getDateDiffInMinutes(invalidResetTime, currentDate);
	   if (getDateDiffInMinutes >= resetPasswordtimeLimitInMinutes) {
	      user.setInvalidResetTime(currentDate);
	      user.setPasswordResetAttempts(Constants.ONE);
	      user.setIsBlocked(Boolean.FALSE);
	   } else {
	      if (passwordResetAttempts < resetPasswordCountLimit && passwordResetAttempts >= 0) {
	         user.setPasswordResetAttempts(++passwordResetAttempts);
	      } 
	      if (passwordResetAttempts >= resetPasswordCountLimit) {
	         user.setIsBlocked(Boolean.TRUE);
	         user.setPasswordResetAttempts(Constants.ZERO);
	         user.setIsPasswordResetEnabled(Boolean.FALSE);
	         user.setBlockedDate(currentDate);
	         userRepository.save(user);
	         return true;
	      }
	   }
	   userRepository.save(user);
	   return false;
	}


	/**
	 * {@inheritDoc}
	 */
	@Transactional
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.getUserByUsername(username, Boolean.TRUE);
		Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
		for (Role role : user.getRoles()) {
			grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
		}
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				grantedAuthorities);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean isForgetPasswordLimitExceed(String username, boolean isFromCreation) {
	   User user = userRepository.getUserByUsername(username, true);
	   if (null == user) {
	      return Boolean.FALSE;
	   }
	   return checkForgotPasswordLimitExceed(user, isFromCreation);
	}
	
	/**
	 * <p>
	 * Common method both api like limit check and forgot password.
	 * </p>
	 * @param user - User object
	 * @param isFromCreation - From user cration
	 * @return Trur or False
	 */
	private Boolean checkForgotPasswordLimitExceed(User user, boolean isFromCreation) {
		int forgotPasswordCount = user.getForgetPasswordCount();
		Date forgotPasswordTime = DateUtil.formatDate(user.getForgetPasswordTime());
		Date currentDate = DateUtil.formatDate(new Date());
		long getDateDiffInMinutes = DateUtil.getDateDiffInMinutes(forgotPasswordTime, currentDate);
		if (getDateDiffInMinutes >= forgotPasswordtimeLimitInMinutes) {
			user.setForgetPasswordTime(currentDate);
			user.setForgetPasswordCount(Constants.ONE);
			user.setIsBlocked(Boolean.FALSE);
		} else {
			if (forgotPasswordCount < forgotPasswordCountLimit && forgotPasswordCount >= 0 && !isFromCreation) {
				user.setForgetPasswordCount(++forgotPasswordCount);
			}
			if (forgotPasswordCount >= forgotPasswordCountLimit) {
				user.setIsBlocked(Boolean.TRUE);
				user.setForgetPasswordCount(Constants.ZERO);
				user.setBlockedDate(currentDate);
				userRepository.save(user);
				return Boolean.TRUE;
			}
		}
		userRepository.save(user);
		return Boolean.FALSE;
	}
	
	@Override
	public void clearApiPermissions() {
		authenticationFilter.apiPermissionMap.clear();
	}
	
	/**
	 * This method is used to generate secret key
	 *
	 * @return Key - secret key
	 */
	private Key secretKeySpecCreation() {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Constants.AES_KEY_TOKEN);
		return new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
	}
	
	/**
	 * This method is used to get notification information
	 * 
	 * @return String - notification information
	 */
	private String getNotificationInfo() {
		String ipInfo = Constants.EMPTY;
		ServiceInstance instance = null;
		try {
			List<ServiceInstance> instanceList = discoveryClient.getInstances(Constants.NOTIFICATION_INSTANCE);
			if (!instanceList.isEmpty()) {
				instance = instanceList.get(Constants.ZERO);
			}
			if (null != instance) {
				ipInfo = instance.getUri().toString();

			}
		} catch (Exception e) {
			e.getMessage();
		}
		return ipInfo;
	}

}
