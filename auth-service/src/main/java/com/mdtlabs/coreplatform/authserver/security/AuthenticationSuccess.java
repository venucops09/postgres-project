package com.mdtlabs.coreplatform.authserver.security;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.logger.Logger;
import com.mdtlabs.coreplatform.common.model.dto.AuthUserDTO;
import com.mdtlabs.coreplatform.common.model.entity.Organization;
import com.mdtlabs.coreplatform.common.model.entity.UserToken;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;
import com.mdtlabs.coreplatform.authservice.repository.UserRepository;

/**
 * <p>
 * <tt>AuthenticationSuccess</tt> Sent to successful authentication.
 * </p>
 * 
 * @author Vigneshkumar Created on 16 Oct 2020
 *
 */
public class AuthenticationSuccess extends SimpleUrlAuthenticationSuccessHandler {

	private RSAPublicKey publicRsaKey;

	@Value("${app.public-key}")
	private String publicKey;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private GenericRepository<UserToken> genericRepository;

	public void init() {
		try {
			Resource resource = new ClassPathResource(publicKey);
			byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
			X509EncodedKeySpec spec = new X509EncodedKeySpec(bdata);
			KeyFactory kf = KeyFactory.getInstance(Constants.RSA); 
			this.publicRsaKey = (RSAPublicKey) kf.generatePublic(spec);
		} catch (Exception e) {
			Logger.logError(Constants.EXCEPTION_TOKEN_UTILS, e); 
		}

	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) {
		if (!response.isCommitted()) {
			response.setStatus(HttpStatus.OK.value());
			response.setContentType(Constants.CONTENT_TEXT_TYPE);
			response.setHeader(Constants.CACHE_HEADER_NAME, Constants.CACHE_HEADER_VALUE);
			response.setHeader(Constants.ACCESS_CONTROL_EXPOSE_HEADERS, Constants.AUTHORIZATION);
			try {
				AuthUserDTO user = getLoggedInUser();
				if (user != null) {
					user.setCurrentDate(new Date().getTime());
					ObjectWriter objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
					String json = objectWriter.writeValueAsString(user);
					response.getWriter().write(json);
					responseHeaderUser(response, user);
				} else {
					response.getWriter().write(Constants.INVALID_USER_ERROR); 
				}
			} catch (IOException e) {
				Logger.logError(Constants.LOGIN_ERROR+ e);
			}

		}
		clearAuthenticationAttributes(request);
	}

	/**
	 * This method is used to construct response header of the user
	 * 
	 * @param response - http servlet reponse is passed
	 * @param user     - user information is passed through DTO
	 * @param maxRole  - role of the corresponding user being passed
	 */
	private void responseHeaderUser(HttpServletResponse response, AuthUserDTO user) {
		init();
		Map<String, Object> userInfo = new ObjectMapper().convertValue(user, Map.class);
		String authToken = null;
		String refreshToken = null;
		try {
			authToken = authTokenCreation(user, userInfo);
			refreshToken = refreshTokenCreation(user);
		} catch (JOSEException execption) {
			Logger.logError(Constants.ERROR_JWE_TOKEN, execption);
		}
		createUserToken(user.getId(), authToken, refreshToken);
		response.setHeader(Constants.AUTHORIZATION, authToken);
		response.setHeader(Constants.REFRESH_TOKEN, refreshToken);
	}

	/**
	 * This method is used to create authorization token
	 * 
	 * @param user     - user information is passed through DTO
	 * @param userInfo - user data is passed in map format
	 * @return - String - jwt encrypted authorization token
	 * @throws JOSEException
	 */
	private String authTokenCreation(AuthUserDTO user, Map<String, Object> userInfo) throws JOSEException {
		List<Long> tenantIds = new ArrayList<>();
//		for (Organization organization : user.getTenants()) {
//			tenantIds.add(organization.getId());
//		}
		String tenantData = tenantIds.stream().map(String::valueOf).collect(Collectors.joining(","));
		JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();
		claimsSet.issuer(Constants.TOKEN_ISSUER);
		claimsSet.subject(Constants.AUTH_TOKEN_SUBJECT);
		claimsSet.claim(Constants.USER_ID_PARAM, user.getId());
//		claimsSet.claim(Constants.TENANT_IDS_CLAIM, tenantData);
		claimsSet.claim(Constants.USER_DATA, userInfo);
		claimsSet.claim(Constants.APPLICATION_TYPE, Constants.WEB);
		claimsSet.expirationTime(Date.from(ZonedDateTime.now().plusMinutes(Constants.AUTH_TOKEN_EXPIRY_MINUTES).toInstant()));
		claimsSet.notBeforeTime(new Date());
		claimsSet.jwtID(UUID.randomUUID().toString());
		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet.build());
		RSAEncrypter encrypter = new RSAEncrypter(this.publicRsaKey);
		jwt.encrypt(encrypter);
		return Constants.BEARER.concat(jwt.serialize());
	}

	/**
	 * This method is used to create refresh token
	 * 
	 * @param user - user information is passed through DTO
	 * @return - String - jwt encrypted refresh token
	 * @throws JOSEException
	 */
	private String refreshTokenCreation(AuthUserDTO user) throws JOSEException {
		JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();
		claimsSet.issuer(Constants.TOKEN_ISSUER);
		claimsSet.subject(Constants.REFRESH_TOKEN_SUBJECT);
		claimsSet.claim(Constants.USER_ID_PARAM, user.getId());
		claimsSet.claim(Constants.APPLICATION_TYPE, Constants.WEB);
		claimsSet.expirationTime(Date.from(ZonedDateTime.now().plusHours(Constants.REFRESH_TOKEN_EXPIRY_HOURS).toInstant())); 
		claimsSet.notBeforeTime(new Date());
		claimsSet.jwtID(UUID.randomUUID().toString());
		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet.build());
		RSAEncrypter encrypter = new RSAEncrypter(this.publicRsaKey);
		jwt.encrypt(encrypter);
		return Constants.BEARER.concat(jwt.serialize());
	}

	/**
	 * To update user token
	 * 
	 * @param userId          - id of the user
	 * @param jwtToken        - jwt token of the logged in user
	 * @param jwtRefreshToken - refresh token of the logged in user
	 */
	private void createUserToken(long userId, String jwtToken, String jwtRefreshToken) {
		UserToken userToken = new UserToken();
		userToken.setUserId(userId);
		userToken.setAuthToken(jwtToken);
		userToken.setRefreshToken(jwtRefreshToken);
		genericRepository.save(userToken);
	}

	/**
	 * To get logged in user details
	 * 
	 * @return UserDTO - user information
	 */
	private AuthUserDTO getLoggedInUser() {
		if (null == SecurityContextHolder.getContext() || null == SecurityContextHolder.getContext().getAuthentication()
				|| null == SecurityContextHolder.getContext().getAuthentication().getPrincipal()) {
			return null;
		}
		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(Constants.ANONYMOUS_USER)) {
			return null;
		}
		return new ModelMapper().map(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), AuthUserDTO.class);
	}

}
