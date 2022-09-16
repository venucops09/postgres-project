package com.project.telecounselor.authserver.security;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.project.telecounselor.common.Constants;
import com.project.telecounselor.common.logger.TelecounselorLogger;
import com.project.telecounselor.model.dto.UserDTO;
import com.project.telecounselor.model.entity.Role;

/**
 * <p>
 * <tt>AuthenticationSuccess</tt> Sent to successful authentication.
 * </p>
 * 
 * @author Vigneshkumar Created on 16 Oct 2020
 *
 */
public class AuthenticationSuccess extends SimpleUrlAuthenticationSuccessHandler {

	private static String CONTENT_TYPE = "text/x-json;charset=UTF-8";
	private static String CACHE_HEADER_NAME = "Cache-Control";
	private static String CACHE_HEADER_VALUE = "no-cache";

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationSuccess.class);
	
	private RestTemplate restService = new RestTemplate();
	
	private RSAPublicKey publicRsaKey;
	
	@Value("${app.public-key}")
	private String publicKey;

	@Autowired
	private org.springframework.cloud.client.discovery.DiscoveryClient discoveryClient;

	public void init() {
		try {			
			Resource resource = new ClassPathResource(publicKey);
			byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
		    X509EncodedKeySpec spec = new X509EncodedKeySpec(bdata);
		    KeyFactory kf = KeyFactory.getInstance("RSA");
		    this.publicRsaKey = (RSAPublicKey) kf.generatePublic(spec);
		} catch (Exception e) {
			LOG.error("Exception occured while loading token utills", e);
		}

	}

	/**
	 * To handle the business logic on authentication failure.
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) {
		request.getSession().setMaxInactiveInterval(60 * 60 * Constants.INACTIVE_SESSION_EXPIRE_HOURS);
		if (!response.isCommitted()) {
			response.setStatus(HttpStatus.OK.value());
			response.setContentType(CONTENT_TYPE);
			response.setHeader(CACHE_HEADER_NAME, CACHE_HEADER_VALUE);
			response.setHeader("Access-Control-Expose-Headers", "Authorization");
			try {

				UserDTO user = getLoggedInUser();
				if (user != null) {
					user.setCurrentDate(new Date().getTime());
					ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String json = objectWriter.writeValueAsString(user);
					response.getWriter().write(json);

					// We will sign our JWT with our ApiKey secret
					/*
					 * byte[] apiKeySecretBytes =
					 * DatatypeConverter.parseBase64Binary(Constants.AES_KEY); Key secretKeySpec =
					 * new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
					 */
					String maxRole = "";
					for (Role role : user.getRoles()) {
						// Roles priorty need to added
					}
					responseHeaderUser(response, user, maxRole);
				} else {
					response.getWriter().write("{ \"error\": \"Invalid User\"}");
				}
			} catch (IOException e) {
				TelecounselorLogger.logError("Login Error " + e);
			}

		}
		clearAuthenticationAttributes(request);
	}

	/**
	 * <p>
	 * This method is used for
	 * </p>
	 * @throws JOSEException 
	 *
	 */
	private void responseHeaderUser(HttpServletResponse response, UserDTO user, String maxRole) {
		init();
		// Set jwt token in response header
		@SuppressWarnings("unchecked")
		Map<String, Object> userInfo = new ObjectMapper().convertValue(user, Map.class);
		String authToken = null;
		String refreshToken =null;
		try {
			authToken = authTokenCreation(user, userInfo);
			refreshToken = refreshTokenCreation(user);
		} catch (JOSEException execption) {
			TelecounselorLogger.logError("Error while creating jwe token ", execption);
		}
		updateUserToken(user.getId(), authToken, refreshToken);
		
		response.setHeader(org.springframework.http.HttpHeaders.AUTHORIZATION, authToken);
		response.setHeader(Constants.REFRESH_TOKEN, refreshToken);
	}
	
	private String authTokenCreation(UserDTO user, Map<String, Object> userInfo) throws JOSEException {
		JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();
		claimsSet.issuer(Constants.TELECOUNSELOR_TOKEN_ISSUER);
		claimsSet.subject(Constants.TELECOUNSELOR_AUTH_TOKEN_SUBJECT);

		// User specified claims
		claimsSet.claim("userId", user.getId());
		claimsSet.claim("userData", userInfo);
		claimsSet.claim("applicationType", Constants.TELECOUNSELOR_WEB);

		claimsSet.expirationTime(Date.from(ZonedDateTime.now().plusHours(Constants.EXPIRY_HOURS).toInstant()));
		claimsSet.notBeforeTime(new Date());
		claimsSet.jwtID(UUID.randomUUID().toString());

		// Create the JWE header and specify:
		// A128GCMKW as the encryption algorithm
		// 128-bit AES/GCM as the encryption method
		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);

		// Initialized the EncryptedJWT object
		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet.build());

		// Create an RSA encrypted with the specified public RSA key
		RSAEncrypter encrypter = new RSAEncrypter(this.publicRsaKey);

		// Doing the actual encryption
		jwt.encrypt(encrypter);

		// Serialize to JWT compact form
		return jwt.serialize();
	}
	
	private String refreshTokenCreation(UserDTO user) throws JOSEException {
		JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();
		claimsSet.issuer(Constants.TELECOUNSELOR_TOKEN_ISSUER);
		claimsSet.subject(Constants.TELECOUNSELOR_REFRESH_TOKEN_SUBJECT);

		// User specified claims
		claimsSet.claim("userId", user.getId());
		claimsSet.claim("applicationType", Constants.TELECOUNSELOR_WEB);

		claimsSet.expirationTime(Date.from(ZonedDateTime.now().plusDays(30).toInstant()));
		claimsSet.notBeforeTime(new Date());
		claimsSet.jwtID(UUID.randomUUID().toString());

		// Create the JWE header and specify:
		// A128GCMKW as the encryption algorithm
		// 128-bit AES/GCM as the encryption method
		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);

		// Initialized the EncryptedJWT object
		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet.build());

		// Create an RSA encrypted with the specified public RSA key
		RSAEncrypter encrypter = new RSAEncrypter(this.publicRsaKey);

		// Doing the actual encryption
		jwt.encrypt(encrypter);

		// Serialize to JWT compact form
		return jwt.serialize();
	}

	/**
	 * To update user token
	 * 
	 * @param userId
	 * @param jwtToken
	 * @param jwtRefreshToken
	 */
	private void updateUserToken(long userId, String jwtToken, String jwtRefreshToken) {
		Map<String, String> userInfoMap = new HashMap<>();
		userInfoMap.put("jwtToken", jwtToken);
		userInfoMap.put("jwtRefreshToken", jwtRefreshToken);
		Map<String, Long> params = new HashMap<>();
		params.put("id", userId);
		// String userIp = getUserInfo()+"/user-service/user";
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, Constants.LOGIN_USER_AUTH);
		HttpEntity<Object> entity = new HttpEntity<Object>(userInfoMap, headers);

		ResponseEntity<Map> userResponse = restService
				.exchange(getUserInfo() + "/user-service/user/user-token/" + userId, HttpMethod.PUT, entity, Map.class);
		// restService.put(userService + "/user-token/{id}", userInfoMap, params);
	}

	/**
	 * To get loggedin user
	 * 
	 * @return UserDTO
	 */
	private UserDTO getLoggedInUser() {
		if (null == SecurityContextHolder.getContext() || null == SecurityContextHolder.getContext().getAuthentication()
				|| null == SecurityContextHolder.getContext().getAuthentication().getPrincipal()) {
			return null;
		}
		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
			return null;
		}

		return new ModelMapper().map(SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
				UserDTO.class);
	}

	/**
	 * This method is used to get the user service URL from service discovery.
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

}
