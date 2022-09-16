package com.project.telecounselor.userservice.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.spec.SecretKeySpec;
import javax.transaction.Transactional;
import javax.xml.bind.DatatypeConverter;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
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

import com.ctc.wstx.shaded.msv_core.verifier.jarv.Const;
import com.project.telecounselor.common.Constants;
import com.project.telecounselor.exception.TelecounselorValidation;
import com.project.telecounselor.model.dto.EmailDTO;
import com.project.telecounselor.model.dto.UserDTO;
import com.project.telecounselor.model.entity.Country;
import com.project.telecounselor.model.entity.EmailTemplate;
import com.project.telecounselor.model.entity.EmailTemplateValue;
import com.project.telecounselor.model.entity.Notification;
import com.project.telecounselor.model.entity.Role;
import com.project.telecounselor.model.entity.Timezone;
import com.project.telecounselor.model.entity.User;
import com.project.telecounselor.userservice.message.SuccessCode;
import com.project.telecounselor.userservice.message.SuccessResponse;
import com.project.telecounselor.userservice.repository.CustomRepositoryImpl;
import com.project.telecounselor.userservice.repository.UserRepository;
import com.project.telecounselor.userservice.service.RoleService;
import com.project.telecounselor.userservice.service.UserService;
import com.project.telecounselor.util.UserContextHolder;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import com.project.telecounselor.common.logger.TelecounselorLogger;
import com.project.telecounselor.common.util.CommonUtil;
import org.springframework.web.client.RestTemplate;
import org.modelmapper.ModelMapper;

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
	private org.springframework.cloud.client.discovery.DiscoveryClient discoveryClient;

	private RestTemplate restService = new RestTemplate();

	@Autowired
	private RoleService roleService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CustomRepositoryImpl customRepositoryImpl;

	private ModelMapper modelMapper = new ModelMapper();

	@Value("${app.page-count}")
	private int gridDisplayValue;

	@Value("${app.time-limit-in-hour}")
	private int timeLimitInHour;

	@Value("${app.login-count-limit}")
	private int loginCountLimit;

	@Value("${app.forget-password-count-limit}")
	private int forgetPasswordCountLimit;

	@Value("${app.mail-user}")
	private String MAIL_USER;

	@Value("${app.reset-password-count-limit}")
	private int resetPasswordCountLimit;

	@Value("${app.email-app-url}")
	private String appUrl;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

	/**
	 * {@inheritDoc}
	 */
	public User addUser(User user) {
		UserDTO userDto = UserContextHolder.getUserDto();
		if (null != user.getRoles()) {
			if (null != getUserByUsername(user.getUsername())) {
				throw new TelecounselorValidation(5002);
			}
			Set<Long> roleIds = user.getRoles().stream().map(Role::getId).collect(Collectors.toSet());
			user.setRoles(roleService.getRolesByIds(roleIds));
			user.setTenantId(userDto.getTenantId());
			user.setCreatedBy(userDto.getId());
			user.setUpdatedBy(userDto.getId());
			user.setForgetPasswordCount(0);
			User newUser = customRepositoryImpl.save(user);
			resetOrUpdatePassWord(user.getUsername(), true);
			return newUser;
		}
		throw new TelecounselorValidation(11000);
	}

	/**
	 * This method is used to compare username and password
	 * 
	 * @param username
	 * @param password
	 * @throws NoSuchAlgorithmException
	 */
	private void passwordCheck(String username, String password) throws NoSuchAlgorithmException {
			String salt=Constants.SALT_KEY;
		  MessageDigest md = MessageDigest.getInstance(Constants.HASHING_CODE);
		  md.update(salt.getBytes(StandardCharsets.UTF_8));
		  byte[] bytes = md.digest(username.getBytes(StandardCharsets.UTF_8));
		  StringBuilder sb = new StringBuilder();
		  for(int i=0; i< bytes.length ;i++){
		      sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		  }
		  if(password.equals(sb.toString())){
		  	TelecounselorLogger.logError(CommonUtil.constructString(Constants.PASSWORD_ERROR));
				throw new TelecounselorValidation(1103);
		  }
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getAllUsers(int pageNumber) {
		Pageable pageable = PageRequest.of(pageNumber, gridDisplayValue);
		Page<User> users = userRepository.getAllUser(pageable);
		return users.stream().filter(user -> user.getIsActive()).collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getAllUsersWithInOrganization(int pageNumber) {
		UserDTO userDTO = UserContextHolder.getUserDto();
		Pageable pageable = PageRequest.of(pageNumber - 1, gridDisplayValue);
		Set<Long> roleIds = userDTO.getRoles().stream().map(Role::getId).collect(Collectors.toSet());
		Page<User> users = userRepository.getAllUsersWithInOrganization(true, userDTO.getTenantId(), roleIds, pageable);
		return users.stream().filter(user -> user.getIsActive()).collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	public User updateUser(User user) {
		UserDTO userDto = UserContextHolder.getUserDto();
		User exisitingUserByUsername = getUserByUsernameNotById(user.getUsername(), user.getId());
		if (null != exisitingUserByUsername) {
			throw new TelecounselorValidation(5002);
		}
		User exisitingUser = getAllUserById(user.getId());
		if (null == exisitingUser) {
			throw new TelecounselorValidation(5003);
		}
		user.setUpdatedBy(userDto.getId());
		user.setUpdatedAt(CommonUtil.formatDate(CommonUtil.getCurrentTimeStamp()));
		user.setId(user.getId());
		return customRepositoryImpl.save(user, exisitingUser);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deleteUserById(long userId) {
		UserDTO userDto = UserContextHolder.getUserDto();
		if (null != userDto.getRoles()) {
			User user = getUserById(userId);
			if (null != user) {
				user.setIsActive(false);
				user.setDeleted(true);
				user.setUpdatedBy(userDto.getId());
				customRepositoryImpl.delete(user);
				// userRepository.updateUserStatusById(Boolean.FALSE, userId);
				return true;
			}
			return false;
		}
		throw new TelecounselorValidation(11000);
	}

	/**
	 * {@inheritDoc}
	 */
	public User getUserByUsernameNotById(String username, long userId) {
		return userRepository.getUserByUsernameNotById(username, userId, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public User getUserById(long userId) {
		return userRepository.getUserById(userId, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public User getUserByUsername(String username) {
		return userRepository.getUserByUsername(username, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public User getAllUserByUsername(String username) {
		return userRepository.getAllUserByUsername(username);
	}

	/**
	 * {@inheritDoc}
	 */
	public User getAllUserById(long id) {
		return userRepository.getAllUserById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean updateUserPassword(String token, String password) {
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Constants.AES_KEY_TOKEN);
		Key secretKeySpec = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
		Claims body = Jwts.parser().setSigningKey(secretKeySpec).parseClaimsJws(token).getBody();
		String userName = (String) body.get(Constants.USERNAME);
		User user = userRepository.getUserByUsername(userName, true);
		String oldPassword = user.getPassword();
		String forgetPasswordToken = user.getForgetPasswordToken();
		if (null == user || !token.equals(user.getForgetPasswordToken())) {
			return false;
		}
		user.setForgetPasswordToken(null);
		if(null != oldPassword &&  oldPassword.equals(password)) {
			TelecounselorLogger.logError(CommonUtil.constructString(Constants.SAME_PASSWORD));
			throw new TelecounselorValidation(1102);
		}
		try {
			passwordCheck(user.getUsername(), password);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		user.setPassword(password);
		customRepositoryImpl.updatePassword(user, oldPassword, forgetPasswordToken);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean resetOrUpdatePassWord(String emailId, boolean isFromCreation) {
		User user = userRepository.getUserByUsername(emailId, true);
		if (null == user) {
			return false;
		}
		String forgetPasswordToken = user.getForgetPasswordToken();
		int existingForgetPasswordCount = user.getForgetPasswordCount();


		if (user.getIsBlocked()) {
			TelecounselorLogger.logError(CommonUtil.constructString(Constants.ERROR_USER_BLOCKED));
			throw new TelecounselorValidation(1101);
		}
		boolean isForgetPasswordLimitExceed = isForgetPasswordLimitExceed(emailId);
		if (isForgetPasswordLimitExceed) {
			TelecounselorLogger.logError(CommonUtil.constructString(Constants.ERROR_USER_BLOCKED));
			throw new TelecounselorValidation(1101);
		}

		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Constants.AES_KEY_TOKEN);
		Key secretKeySpec = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
		Map<String, Object> userInfo = new HashMap<>();
		userInfo.put(Constants.USERNAME, user.getUsername());
		JwtBuilder jwtBuilder = Jwts.builder().setClaims(userInfo).signWith(signatureAlgorithm, secretKeySpec);
		String jwtToken = jwtBuilder.setId(String.valueOf(user.getId()))
				.setExpiration(Date.from(ZonedDateTime.now().plusHours(24).toInstant()))
				.setIssuedAt(Date.from(ZonedDateTime.now().toInstant())).setIssuer(Constants.ISSUER).compact();
		try {
			sendEmail(user, jwtToken, isFromCreation);
			user.setForgetPasswordToken(jwtToken);
			customRepositoryImpl.updatePasswordToken(user, jwtToken, forgetPasswordToken, existingForgetPasswordCount);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Boolean changeOrUpdatePassword(Map<String, String> userInfo) {
		User user = userRepository.getUserByUsername(userInfo.get(Constants.USERNAME), true);
		String oldPassword = userInfo.get(Constants.OLD_PASSWORD);
		// BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		if (!user.getPassword().equals(oldPassword)) {
			return false;
		}

		if (user.getIsBlocked()) {
			TelecounselorLogger.logError(CommonUtil.constructString(Constants.ERROR_USER_BLOCKED));
			throw new TelecounselorValidation(1101);
		}
		boolean isResetPasswordLimitExceed = isResetPasswordLimitExceed(userInfo.get(Constants.USERNAME));
		if (isResetPasswordLimitExceed) {
			TelecounselorLogger.logError(CommonUtil.constructString(Constants.ERROR_USER_BLOCKED));
			throw new TelecounselorValidation(1101);
		}

		String newPassword = userInfo.get(Constants.NEW_PASSWORD);
		if(oldPassword.equals(newPassword)) {
			TelecounselorLogger.logError(CommonUtil.constructString(Constants.SAME_PASSWORD));
			throw new TelecounselorValidation(1102);
		}
		try {
			passwordCheck(user.getUsername(), newPassword);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		user.setPassword(newPassword);
		customRepositoryImpl.changePassword(user, oldPassword, newPassword);
		User userpwd = userRepository.getUserByUsername(userInfo.get(Constants.USERNAME), true);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getUserByFirstName(String name) {
		UserDTO userDTO = UserContextHolder.getUserDto();
		Set<Long> roleIds = userDTO.getRoles().stream().map(Role::getId).collect(Collectors.toSet());
		List<User> users = userRepository.getUserByFirstName(name, userDTO.getTenantId(), roleIds);
		return users.stream().filter(user -> user.getIsActive()).collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	public int updateUserToken(long id, Map<String, String> userInfo) {
		return userRepository.updateUserToken(id, userInfo.get("jwtToken"), userInfo.get("jwtRefreshToken"));
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getUserByRoleName(long id) {
		UserDTO user = UserContextHolder.getUserDto();
		return userRepository.getUserByRoleName(id, user.getTenantId());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean deletedAllUserInTenant(long tenantId) {
		UserDTO userDTO = UserContextHolder.getUserDto();
		List<User> users = getAllUserByTenantId(tenantId);
		for (User user : users) {
			user.setIsActive(false);
			user.setUpdatedBy(userDTO.getId());
		}
		customRepositoryImpl.deleteAllUserInTenant(tenantId);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getTenantTotalSize() {
		UserDTO userDTO = UserContextHolder.getUserDto();
		Set<Long> roleIds = userDTO.getRoles().stream().map(Role::getId).collect(Collectors.toSet());
		List<User> users = userRepository.getAllUsersWithInOrganization(true, userDTO.getTenantId(), roleIds);
		return users.stream().filter(user -> user.getIsActive()).collect(Collectors.toList()).size();
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, List<Map<String, Object>>> getUserMeta() {
		Map<String, List<Map<String, Object>>> meta = new HashMap<>();
		meta.put("role", getRoleMeta());
		return meta;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, Object>> getRoleMeta() {
		List<Map<String, Object>> metaRoles = new ArrayList<>();
		List<Role> roles = roleService.getAllRoles();
		for (Role role : roles) {
			Map<String, Object> metaRole = new HashMap<>();
			metaRole.put("id", role.getId());
			metaRole.put("name", role.getName());
			metaRoles.add(metaRole);
		}
		return metaRoles;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getAllUserByTenantId(long tenantId) {
		return userRepository.getAllUserByTenantId(tenantId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<User> getAllUserInTenant() {
		UserDTO userDTO = UserContextHolder.getUserDto();
		return userRepository.getAllUserByTenantId(userDTO.getTenantId());
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendEmail(User user, String jwtToken, boolean isFromCreation) {
		EmailTemplate emailTemplate = new EmailTemplate();
		Notification notification = new Notification();
		String notificationIp = getNotificationInfo() + "/notification-service/email";
		Map<String, String> data = new HashMap<String, String>();
		try {
			EmailDTO emailDto = new EmailDTO();
			if (isFromCreation) {
				HttpEntity<String> entity = CommonUtil.getCurrentEntity();
				ResponseEntity<Map> emailTemplateResponse = restService.exchange(
						notificationIp + "/email-type/" + Constants.NEW_USER_CREATION, HttpMethod.GET, entity,
						Map.class);
				emailTemplate = modelMapper.map(emailTemplateResponse.getBody().get("entity"), EmailTemplate.class);
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
				notification = new Notification(Constants.CREATION_NOTIFICATION_SUBJECT, Constants.USER_CREATION,
						user.getUsername());
			} else {
				ResponseEntity<Map> emailTemplateResponse = restService
						.getForEntity(notificationIp + "/email-type/" + Constants.FORGOT_PASSWORD_USER, Map.class);
				emailTemplate = modelMapper.map(emailTemplateResponse.getBody().get("entity"), EmailTemplate.class);
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
				emailDto.setFromName(MAIL_USER);
				emailDto.setSubject(Constants.FORGOT_NOTIFICATION_SUBJECT);
				emailDto.setTo(user.getUsername());
				emailDto.setFrom(MAIL_USER);
				notification = new Notification(Constants.FORGOT_NOTIFICATION_SUBJECT, Constants.FORGOT_PASSWORD,
						user.getUsername());
			}
			HttpHeaders header = new HttpHeaders();
			header.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<EmailDTO> emailEntity = new HttpEntity<EmailDTO>(emailDto, header);
			ResponseEntity<Map> emailResponse = restService.exchange(notificationIp + "/send-email", HttpMethod.POST,
					emailEntity, Map.class);
			createNotificationAndSendEmail(emailDto,
					modelMapper.map(emailResponse.getBody().get("entity"), Boolean.class));
		} catch (Exception e) {
			e.getMessage();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLoginLimitExceed(String username) {
		User user = userRepository.getUserByUsername(username, true);
		User existingUser = userRepository.getUserByUsername(username, true);
		if (null == user) {
			return false;
		}
		int invalidLoginAttempts = user.getInvalidLoginAttempts();
		Date invalidLoginTime = CommonUtil.formatDate(user.getInvalidLoginTime());
		Date currentDate = CommonUtil.formatDate(new Date());
		long differenceInHours = CommonUtil.getDiffInHours(currentDate, invalidLoginTime);
		if (differenceInHours >= timeLimitInHour) {
			user.setInvalidLoginTime(currentDate);
			user.setInvalidLoginAttempts(Constants.ONE);
			user.setIsBlocked(Boolean.FALSE);
			user.setIsActive(Boolean.TRUE);
		} else {
			if (invalidLoginAttempts >= loginCountLimit) {
				user.setIsBlocked(Boolean.TRUE);
				user.setInvalidLoginAttempts(Constants.ZERO);
				user.setIsActive(Boolean.FALSE);
				user.setBlockedDate(currentDate);
				customRepositoryImpl.save(user, existingUser);
				return true;
			} else if (invalidLoginAttempts < loginCountLimit && invalidLoginAttempts >= 0) {
				user.setInvalidLoginAttempts(++invalidLoginAttempts);
			}
		}
		customRepositoryImpl.save(user, existingUser);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isForgetPasswordLimitExceed(String username) {
		User user = userRepository.getUserByUsername(username, true);
		User existingUser = userRepository.getUserByUsername(username, true);
		if (null == user) {
			return false;
		}
		int forgetPasswordCount = user.getForgetPasswordCount();
		Date forgetPasswordTime = CommonUtil.formatDate(user.getForgetPasswordTime());
		Date currentDate = CommonUtil.formatDate(new Date());
		long differenceInMinutes = CommonUtil.getDiffInMinutes(currentDate, forgetPasswordTime);
		long differenceInHours = CommonUtil.getDiffInHours(currentDate, forgetPasswordTime);
		if (differenceInHours >= timeLimitInHour) {
			user.setForgetPasswordTime(currentDate);
			user.setForgetPasswordCount(Constants.ONE);
			user.setIsBlocked(Boolean.FALSE);
			user.setIsActive(Boolean.TRUE);
		} else {
			if (forgetPasswordCount >= forgetPasswordCountLimit) {
				user.setIsActive(Boolean.FALSE);
				user.setIsBlocked(Boolean.TRUE);
				user.setForgetPasswordCount(Constants.ZERO);
				user.setBlockedDate(currentDate);
				customRepositoryImpl.forgetPassword(user);
				return true;
			} else if (forgetPasswordCount < forgetPasswordCountLimit && forgetPasswordCount >= 0) {
				user.setForgetPasswordCount(++forgetPasswordCount);
			}
		}
		customRepositoryImpl.save(user, existingUser);
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isResetPasswordLimitExceed(String username) {
		User user = userRepository.getUserByUsername(username, true);
		User existingUser = userRepository.getUserByUsername(username, true);
		if (null == user) {
			return false;
		}
		int passwordResetAttempts = user.getPasswordResetAttempts();
		Date invalidResetTime = CommonUtil.formatDate(user.getInvalidResetTime());
		Date currentDate = CommonUtil.formatDate(new Date());
		long differenceInHours = CommonUtil.getDiffInHours(currentDate, invalidResetTime);
		if (differenceInHours >= timeLimitInHour) {
			user.setInvalidResetTime(currentDate);
			user.setPasswordResetAttempts(Constants.ONE);
			user.setIsBlocked(Boolean.FALSE);
			user.setIsActive(Boolean.TRUE);
		} else {
			if (passwordResetAttempts >= resetPasswordCountLimit) {
				user.setIsBlocked(Boolean.TRUE);
				user.setPasswordResetAttempts(Constants.ZERO);
				user.setIsActive(Boolean.FALSE);
				user.setIsPasswordResetEnabled(Boolean.FALSE);
				user.setBlockedDate(currentDate);
				customRepositoryImpl.save(user, existingUser);
				return true;
			} else if (passwordResetAttempts < resetPasswordCountLimit && passwordResetAttempts >= 0) {
				user.setPasswordResetAttempts(++passwordResetAttempts);
			}
		}
		customRepositoryImpl.save(user, existingUser);
		return false;
	}

	/**
	 * <p>
	 * This method is used to get the user.
	 * </p>
	 * service URL from service discovery.
	 * 
	 * @return String
	 */
	private String getNotificationInfo() {
		String ipInfo = "";
		ServiceInstance instance = null;
		try {
			List<ServiceInstance> instanceList = new ArrayList<>();
			instanceList = discoveryClient.getInstances("NOTIFICATION");
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
	 * {@inheritDoc}
	 */
	public List<User> getUsersByRolename(String roleName, int pageNumber, int limit) {
		limit = limit > 0 ? limit : gridDisplayValue;
		Pageable pageable = PageRequest.of(pageNumber - 1, limit);
		Page<User> users = userRepository.getUsersByRolename(true, roleName, pageable);
		return users.stream().collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	@Transactional()
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.getUserByUsername(username, true);
		Set<GrantedAuthority> grantedAuthorities = new HashSet<>();
		for (Role role : user.getRoles()) {
			grantedAuthorities.add(new SimpleGrantedAuthority(role.getName()));
		}

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				grantedAuthorities);
	}

	/**
	 * Used to parse email template
	 * 
	 * @param htmlTemplate
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public String parseEmailTemplate(String htmlTemplate, Map<String, String> data) throws Exception {
		if (data != null && !data.isEmpty()) {
			String[] result = new String[1];
			result[0] = htmlTemplate;
			data.forEach((key, value) -> {
				result[0] = result[0].replace("${" + key + "}", value);
			});
			return result[0];
		}
		return htmlTemplate;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Country> getCountryList() {
		List<Country> country = userRepository.getCountryList();
		return country;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Timezone> getTimezoneList() {
		List<Timezone> timezone = userRepository.getTimezoneList();
		return timezone;
	}

	/**
	 * {@inheritDoc}
	 */
	public Country getCountryById(String id) {
		return userRepository.getCountryById(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public Timezone getTimezoneById(String id) {
		return userRepository.getTimezoneById(id);
	}

	public String createNotificationAndSendEmail(EmailDTO email, boolean sendStatus) {
		try {
			Notification notification = new Notification(email.getSubject(), email.getBody(), email.getTo());
			notification.setStatus(
					!sendStatus ? Constants.NOTIFICAION_STATUS_FAILED : Constants.NOTIFICAION_STATUS_PROCESSED);
			String emailIp = getNotificationInfo() + "/notification-service/notification";
			UserDTO userDto = UserContextHolder.getUserDto();
			notification.setCreatedAt(CommonUtil.formatDate(CommonUtil.getCurrentTimeStamp()));
			notification.setCreatedBy(userDto.getId());
			notification.setUpdatedBy(userDto.getId());
			HttpHeaders header = new HttpHeaders();
			HttpEntity<Notification> notificationEntity = new HttpEntity<>(notification, header);
			header.setContentType(MediaType.APPLICATION_JSON);
			ResponseEntity<Map> notificationResponse = restService.exchange(emailIp, HttpMethod.POST,
					notificationEntity, Map.class);
			if (null != notificationResponse.getBody().get("entity")) {
				return Constants.SUCCESS;
			} else {
				return Constants.ERROR;
			}
		} catch (Exception error) {
			return Constants.ERROR;
		}
	}
}