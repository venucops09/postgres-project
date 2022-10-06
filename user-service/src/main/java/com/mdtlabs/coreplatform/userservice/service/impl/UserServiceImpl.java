package com.mdtlabs.coreplatform.userservice.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.mdtlabs.coreplatform.userservice.service.UserService;
import com.mdtlabs.coreplatform.AuthenticationFilter;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.UserContextHolder;
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
import com.mdtlabs.coreplatform.common.model.entity.Role;
import com.mdtlabs.coreplatform.common.model.entity.User;
import com.mdtlabs.coreplatform.common.model.entity.UserToken;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;
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
 * @author VigneshKumar created on Jun 30, 2022
 */
@Service
public class UserServiceImpl implements UserService, UserDetailsService {

	@Autowired
	private DiscoveryClient discoveryClient;

	private RestTemplate restService = new RestTemplate();

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private GenericRepository<UserToken> genericRepository;
	
	@Autowired
	private AuthenticationFilter authenticationFilter;

	private ModelMapper modelMapper = new ModelMapper();

	@Value("${app.page-count}")
	private int gridDisplayValue;

	@Value("${app.login-time-limit-in-hour}")
	private int loginTimeLimitInHour;

	@Value("${app.login-count-limit}")
	private int loginCountLimit;

	@Value("${app.forget-password-count-limit}")
	private int forgetPasswordCountLimit;

	@Value("${app.mail-user}")
	private String mailUser;

	@Value("${app.reset-password-count-limit}")
	private int resetPasswordCountLimit;

	@Value("${app.email-app-url}")
	private String appUrl;

	/**
	 * {@inheritDoc}
	 */
	public User addUser(User user) {
		UserDTO userDto = UserContextHolder.getUserDto();
		if (null != user.getRoles()) {
			if (null != getUserByUsername(user.getUsername())) {
				throw new Validation(1009);
			}
			user.setForgetPasswordCount(Constants.ZERO);
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
		return userRepository.save(user);
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
			return (deletedUser ==null)? Boolean.FALSE: Boolean.TRUE;
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
	public Boolean updatePassword(String token, String password) {
		Key secretKeySpec = secretKeySpecCreation();
		Claims body = Jwts.parser().setSigningKey(secretKeySpec).parseClaimsJws(token).getBody();
		String userName = (String) body.get(Constants.USERNAME);
		User user = userRepository.getUserByUsername(userName, Boolean.TRUE);
		String oldPassword = user.getPassword();
		if (null == user || !token.equals(user.getForgetPasswordToken())) {
			Logger.logError(StringUtil.constructString(Constants.LINK_EXPIRED));
			throw new Validation(3009);
		}
		if (null != oldPassword && oldPassword.equals(password)) {
			Logger.logError(StringUtil.constructString(Constants.SAME_PASSWORD));
			throw new Validation(1012);
		}
		try {
			checkUsernameSameAsPassword(user.getUsername(), password);
		} catch (NoSuchAlgorithmException e) {
			Logger.logError(e);
		}
		user.setPassword(password);
		user.setForgetPasswordToken(null);
		user.setForgetPasswordCount(0);
		userRepository.save(user);
		return Boolean.TRUE;
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
	 * {@inheritDoc}
	 */
	public Boolean forgotPassword(String emailId, boolean isFromCreation) {
		User user = userRepository.getUserByUsername(emailId, Boolean.TRUE);
		if (null == user) {
			return Boolean.FALSE;
		}
		if (Boolean.TRUE.equals(user.getIsBlocked())) {
			Logger.logError(StringUtil.constructString(Constants.ERROR_USER_BLOCKED));
			throw new Validation(1013);
		}
		boolean isForgetPasswordLimitExceed = forgetPasswordLimitExceed(emailId);
		if (isForgetPasswordLimitExceed) {
			Logger.logError(StringUtil.constructString(Constants.ERROR_USER_BLOCKED));
			throw new Validation(1013);
		}
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Constants.AES_KEY_TOKEN);
		Key secretKeySpec = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put(Constants.USERNAME, user.getUsername());
		JwtBuilder jwtBuilder = Jwts.builder().setClaims(userInfo).signWith(signatureAlgorithm, secretKeySpec);
		String jwtToken = jwtBuilder.setId(String.valueOf(user.getId()))
				.setExpiration(Date.from(ZonedDateTime.now().plusHours(Constants.TWENTY_FOUR).toInstant()))
				.setIssuedAt(Date.from(ZonedDateTime.now().toInstant())).setIssuer(Constants.ISSUER).compact();
		try {
			sendEmail(user, jwtToken, isFromCreation);
			user.setForgetPasswordToken(jwtToken);
			UserContextHolder.setUserDto(modelMapper.map(user, UserDTO.class));
			userRepository.save(user);
		} catch (Exception e) {
			Logger.logError(e);
		}
		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public UserToken createUserToken(long id, Map<String, String> userInfo) {
		UserToken userToken = new UserToken();
		userToken.setUserId(id);
		userToken.setAuthToken(userInfo.get(Constants.JWT_TOKEN));
		userToken.setRefreshToken(userInfo.get(Constants.JWT_REFRESH_TOKEN));
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
		String notificationIp = getNotificationInfo() + "/notification-service/email";
		Map<String, String> data = new HashMap<>();
		try {
			EmailDTO emailDto = new EmailDTO();
			if (isFromCreation) {
				HttpEntity<String> entity = CommonUtil.getCurrentEntity();
				ResponseEntity<Map> emailTemplateResponse = restService.exchange(
						notificationIp + "/email-type/" + Constants.NEW_USER_CREATION, HttpMethod.GET, entity,
						Map.class);
				if (emailTemplateResponse.getBody() == null
						|| emailTemplateResponse.getBody().get(Constants.ENTITY) == null) {
					throw new Validation(3010);
				} else {
					constructEmail(user, jweToken, data, emailDto, emailTemplateResponse);
				}
			} else {
				ResponseEntity<Map> emailTemplateResponse = restService
						.getForEntity(notificationIp + "/email-type/" + Constants.FORGOT_PASSWORD_USER, Map.class);
				if (emailTemplateResponse.getBody() == null
						|| emailTemplateResponse.getBody().get(Constants.ENTITY) == null) {
					throw new Validation(3010);
				} else {
					constructMail(user, jweToken, data, emailDto, emailTemplateResponse);
				}
				sendEmailThread(notificationIp, emailDto);
			}
		} catch (Exception e) {
			e.getMessage();
		}
	}

	/**
	 * This method is used to construct mail for forget password trigger
	 * 
	 * @param user - user entity
	 * @param jweToken - jwe token
	 * @param data - mail content
	 * @param emailDto - email dto
	 * @param emailTemplateResponse - email template response
	 */
	private void constructMail(User user, String jwtToken, Map<String, String> data, EmailDTO emailDto,
			ResponseEntity<Map> emailTemplateResponse) {
		EmailTemplate emailTemplate= modelMapper.map(emailTemplateResponse.getBody().get(Constants.ENTITY),
				EmailTemplate.class);
		for (EmailTemplateValue emailTemplateValue : emailTemplate.getEmailTemplateValues()) {
			if (Constants.APP_URL_EMAIL.equals(emailTemplateValue.getName())) {
				emailTemplateValue.setValue(appUrl + jwtToken);
				data.put(Constants.APP_URL_EMAIL, appUrl + jwtToken);
			} else if (Constants.FORGET_PASSWORD_TOKEN.equals(emailTemplateValue.getName())) {
				emailTemplateValue.setValue(jwtToken);
				data.put(Constants.FORGET_PASSWORD_TOKEN, jwtToken);
			}
		}
		emailDto.setBody(parseEmailTemplate(emailTemplate.getBody(), data));
		emailDto.setFromName(mailUser);
		emailDto.setSubject(Constants.FORGOT_NOTIFICATION_SUBJECT);
		emailDto.setTo(user.getUsername());
		emailDto.setFrom(mailUser);
		new Notification(Constants.FORGOT_NOTIFICATION_SUBJECT, Constants.FORGOT_PASSWORD,
				user.getUsername());
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
	private void constructEmail(User user, String jwtToken, Map<String, String> data, EmailDTO emailDto,
			ResponseEntity<Map> emailTemplateResponse) {
		EmailTemplate emailTemplate = modelMapper.map(emailTemplateResponse.getBody().get(Constants.ENTITY), EmailTemplate.class);
		for (EmailTemplateValue emailTemplateValue : emailTemplate.getEmailTemplateValues()) {
			if (Constants.APP_URL_EMAIL.equalsIgnoreCase(emailTemplateValue.getName())) {
				data.put(Constants.APP_URL_EMAIL, appUrl + jwtToken);
				emailTemplateValue.setValue(appUrl + jwtToken);
			}
		}
		emailDto.setBody(parseEmailTemplate(emailTemplate.getBody(), data));
		emailDto.setEmailTemplate(emailTemplate);
		emailDto.setSubject(Constants.RESET_NOTIFICATION_SUBJECT);
		emailDto.setTo(user.getUsername());
		new Notification(Constants.RESET_NOTIFICATION_SUBJECT, Constants.USER_CREATION, user.getUsername());
	}

	/**
	 * This method is used to send mail in runnable thread
	 * 
	 * @param notificationIp - notification info
	 * @param emailDto-      email entity
	 */
	private void sendEmailThread(String notificationIp, EmailDTO emailDto) {
		new Thread() {
			@Override
			public void run() {
				HttpHeaders header = new HttpHeaders();
				header.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<EmailDTO> emailEntity = new HttpEntity<>(emailDto, header);
				ResponseEntity<Map> emailResponse = restService.exchange(notificationIp + "/send-email",
						HttpMethod.POST, emailEntity, Map.class);
				if (null != emailResponse.getBody()) {
					createNotificationAndSendEmail(emailDto,
							modelMapper.map(emailResponse.getBody().get(Constants.ENTITY), Boolean.class));
				} else {
					throw new Validation(3011);
				}
			}
		};
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
	public boolean forgetPasswordLimitExceed(String username) {
		User user = userRepository.getUserByUsername(username, Boolean.TRUE);
		if (null == user) {
			return Boolean.FALSE;
		}
		int forgetPasswordCount = user.getForgetPasswordCount();
		Date forgetPasswordTime = DateUtil.formatDate(user.getForgetPasswordTime());
		Date currentDate = DateUtil.formatDate(new Date());
		long differenceInHours = DateUtil.getDiffInHours(currentDate, forgetPasswordTime);
		if (differenceInHours >= loginTimeLimitInHour) {
			user.setForgetPasswordTime(currentDate);
			user.setForgetPasswordCount(Constants.ONE);
			user.setIsBlocked(Boolean.FALSE);
		} else {
			if (forgetPasswordCount < forgetPasswordCountLimit && forgetPasswordCount >= Constants.ZERO) {
				user.setForgetPasswordCount(++forgetPasswordCount);
			}
			if (forgetPasswordCount >= forgetPasswordCountLimit) {
				user.setIsBlocked(Boolean.TRUE);
				user.setForgetPasswordCount(Constants.ZERO);
				user.setBlockedDate(currentDate);
				userRepository.save(user);
				return Boolean.TRUE;
			} else if (forgetPasswordCount >= Constants.ZERO) {
				user.setForgetPasswordCount(++forgetPasswordCount);
			}
		}
		userRepository.save(user);
		return Boolean.FALSE;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean resetPasswordLimitExceed(String username) {
		User user = userRepository.getUserByUsername(username, Boolean.TRUE);
		if (null == user) {
			return Boolean.FALSE;
		}
		int passwordResetAttempts = user.getPasswordResetAttempts();
		Date invalidResetTime = DateUtil.formatDate(user.getInvalidResetTime());
		Date currentDate = DateUtil.formatDate(new Date());
		long differenceInHours = DateUtil.getDiffInHours(currentDate, invalidResetTime);
		if (differenceInHours >= loginTimeLimitInHour) {
			user.setInvalidResetTime(currentDate);
			user.setPasswordResetAttempts(Constants.ONE);
			user.setIsBlocked(Boolean.FALSE);
		} else {
			if (passwordResetAttempts < resetPasswordCountLimit && passwordResetAttempts >= Constants.ZERO) {
				user.setPasswordResetAttempts(++passwordResetAttempts);
			}
			if (passwordResetAttempts >= resetPasswordCountLimit) {
				user.setIsBlocked(Boolean.TRUE);
				user.setPasswordResetAttempts(Constants.ZERO);
				user.setIsPasswordResetEnabled(Boolean.FALSE);
				user.setBlockedDate(currentDate);
				userRepository.save(user);
				return Boolean.TRUE;
			} else if (passwordResetAttempts >= Constants.ZERO) {
				user.setPasswordResetAttempts(++passwordResetAttempts);
			}
		}
		userRepository.save(user);
		return Boolean.FALSE;
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
	 * This method is used to parse email template
	 * 
	 * @param htmlTemplate - html template structure
	 * @param data         - content of email
	 * @return String - parsed html template
	 */
	public String parseEmailTemplate(String htmlTemplate, Map<String, String> data) {
		if (data != null && !data.isEmpty()) {
			String[] result = new String[Constants.ONE];
			result[Constants.ZERO] = htmlTemplate;
			data.forEach(
					(key, value) -> result[Constants.ZERO] = result[Constants.ZERO].replace("${" + key + "}", value));
			return result[Constants.ZERO];
		}
		return htmlTemplate;
	}

	/**
	 * This method is used to create notification and send email
	 * 
	 * @param email      - email entity
	 * @param sendStatus - status of email of whether true or false
	 * @return String - state of sending email of whether success or error
	 */
	public String createNotificationAndSendEmail(EmailDTO email, boolean sendStatus) {
		try {
			Notification notification = new Notification(email.getSubject(), email.getBody(), email.getTo());
			notification.setStatus(
					!sendStatus ? Constants.NOTIFICAION_STATUS_FAILED : Constants.NOTIFICAION_STATUS_PROCESSED);
			String emailIp = getNotificationInfo() + "/notification-service/notification";
			UserDTO userDto = UserContextHolder.getUserDto();
			notification.setCreatedAt(DateUtil.formatDate(CommonUtil.getCurrentTimeStamp()));
			notification.setCreatedBy(userDto.getId());
			notification.setUpdatedBy(userDto.getId());
			HttpHeaders header = new HttpHeaders();
			HttpEntity<Notification> notificationEntity = new HttpEntity<>(notification, header);
			header.setContentType(MediaType.APPLICATION_JSON);
			ResponseEntity<Map> notificationResponse = restService.exchange(emailIp, HttpMethod.POST,
					notificationEntity, Map.class);
			if (null != notificationResponse.getBody()
					&& null != notificationResponse.getBody().get(Constants.ENTITY)) {
				return Constants.SUCCESS;
			} else {
				return Constants.ERROR;
			}
		} catch (Exception error) {
			return Constants.ERROR;
		}
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
			Logger.logError(StringUtil.constructString(Constants.PASSWORD_ERROR));
			throw new Validation(1014);
		}
	}
	
	@Override
	public void clearApiPermissions() {
		authenticationFilter.apiPermissionMap.clear();
	}
}