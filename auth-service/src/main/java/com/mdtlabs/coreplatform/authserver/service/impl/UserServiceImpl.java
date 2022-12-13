package com.mdtlabs.coreplatform.authserver.service.impl;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdtlabs.coreplatform.authserver.service.UserService;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.CustomDateSerializer;
import com.mdtlabs.coreplatform.common.ErrorConstants;
import com.mdtlabs.coreplatform.common.exception.Validation;
import com.mdtlabs.coreplatform.common.logger.Logger;
import com.mdtlabs.coreplatform.common.model.dto.AuthUserDTO;
import com.mdtlabs.coreplatform.common.model.dto.TimezoneDTO;
import com.mdtlabs.coreplatform.common.model.dto.UserDTO;
import com.mdtlabs.coreplatform.common.model.entity.UserToken;
import com.mdtlabs.coreplatform.common.repository.CommonRepository;
import com.mdtlabs.coreplatform.common.service.UserTokenService;
import com.mdtlabs.coreplatform.common.util.DateUtil;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;

import io.jsonwebtoken.ExpiredJwtException;

@Service
public class UserServiceImpl implements UserService {

	private RSAPrivateKey privateRsaKey = null;

	public RSAPublicKey publicRsaKey;

	@Value("${app.private-key}")
	private String privateKey;

	@Value("${app.public-key}")
	private String publicKey;

	@Autowired
	UserTokenService userTokenService;

	@Autowired
	CommonRepository commonRepository;

	org.apache.logging.log4j.Logger log = LogManager.getLogger(UserServiceImpl.class);

	/**
	 * {@inheritDoc}
	 */
	public Map<String, String> generateRefreshToken(long userId, String refreshToken)
			throws ParseException, JsonMappingException, JsonProcessingException {

		String newAuthToken = null;
		String newRefreshToken = null;

		Map<String, String> tokensMap = new HashMap<>();

		UserDTO userDetail = null;

		UserToken userToken = userTokenService.validateRefreshToken(userId,
				refreshToken.substring(Constants.BEARER.length(), refreshToken.length()));

		System.out.println("userToken: " + userToken);

		if (Objects.isNull(userToken)) {
			throw new Validation(3013);
		}

		if (null == privateRsaKey) {
			tokenDecrypt();
		}

		RSADecrypter decrypter = new RSADecrypter(privateRsaKey);
		System.out.println("line 108-----------------------------------");
		EncryptedJWT jwt;
		try {
			jwt = EncryptedJWT.parse(userToken.getAuthToken());
		} catch (ParseException e) {
			Logger.logError(e);
			throw new Validation(3013);
		}
		System.out.println("line 116----------------------------------------");

		try {
			jwt.decrypt(decrypter);
		} catch (JOSEException e) {
			Logger.logError(e);
			throw new Validation(3013);
		}
		System.out.println("line 124------------------------------------");
		EncryptedJWT jwtRefresh;
		try {
			jwtRefresh = EncryptedJWT.parse(refreshToken.substring(Constants.BEARER.length(), refreshToken.length()));
		} catch (ParseException e) {
			Logger.logError(e);
			throw new Validation(3013);
		}
		System.out.println("line 132------------------------------------------");
		try {
			jwtRefresh.decrypt(decrypter);
		} catch (JOSEException e) {
			Logger.logError(e);
			throw new Validation(3013);
		}
		System.out.println("line 139----------------------------------------------");
		String rawJson = String.valueOf(jwt.getJWTClaimsSet().getClaim(Constants.USER_DATA));
		ObjectMapper objectMapper = new ObjectMapper();

		userDetail = objectMapper.readValue(rawJson, UserDTO.class);
		userDetail.setAuthorization(userToken.getAuthToken());
		System.out.println("line 145------------------------------------------");
//		if (null != userDetail.getTimezone()) {
////			CustomDateSerializer.USER_ZONE_ID = userDetail.getTimezone().getOffset();
//			ModelMapper modelMapper = new ModelMapper();
//			CustomDateSerializer.USER_ZONE_ID = modelMapper.map(userDetail.getTimezone().getId(), TimezoneDTO.class)
//					.getOffset();
//		}

		DateFormat pstFormat = new SimpleDateFormat(Constants.JSON_DATE_FORMAT);
		Date currentDate = pstFormat.parse(pstFormat.format(DateUtil.formatDate(new Date())));
		Date expDateRefresh = pstFormat.parse(pstFormat.format(jwtRefresh.getJWTClaimsSet().getClaim(Constants.EXP)));

		if ((expDateRefresh.getTime() - currentDate.getTime()) / Constants.THOUSAND < Constants.ZERO) {
			System.out.println("line 158---------------------------------------");
			throw new ExpiredJwtException(null, null, ErrorConstants.TOKEN_EXPIRED);
		} else {
			System.out.println("line 161-------------------------------------");
			Map<String, Object> userInfo = new ObjectMapper().convertValue(userDetail, Map.class);
			try {
				generateKey();
				newAuthToken = authTokenCreation(userDetail, userInfo);
				newRefreshToken = refreshTokenCreation(userDetail);
				updateUserToken(userDetail.getId(), newAuthToken, newRefreshToken);
			} catch (JOSEException e) {
				Logger.logError(ErrorConstants.JOSE_EXCEPTION + e);
			} catch (Exception e) {
				Logger.logError(ErrorConstants.EXCEPTION_DURING_TOKEN_UTIL, e);
			}
			tokensMap.put("authToken", newAuthToken);
			tokensMap.put("refreshToken", newRefreshToken);
		}
		return tokensMap;
	}

	/**
	 * <p>
	 * Decrypt given jwe token using private key.
	 * </p>
	 */
	private void tokenDecrypt() {
		try {
			Resource resource = new ClassPathResource(privateKey);
			byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
			PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(bdata);
			KeyFactory kf = KeyFactory.getInstance(Constants.RSA);
			this.privateRsaKey = (RSAPrivateKey) kf.generatePrivate(privateKeySpec);
		} catch (Exception execption) {
			Logger.logError(ErrorConstants.EXCEPTION_DURING_TOKEN_UTIL, execption);
		}

	}

	private void generateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		Resource resource = new ClassPathResource(publicKey);
		byte[] bdata = FileCopyUtils.copyToByteArray(resource.getInputStream());
		X509EncodedKeySpec spec = new X509EncodedKeySpec(bdata);
		KeyFactory kf = KeyFactory.getInstance(Constants.RSA);
		publicRsaKey = (RSAPublicKey) kf.generatePublic(spec);
	}

	/**
	 * This method is used to create authorization token
	 * 
	 * @param user     - user information is passed through DTO
	 * @param userInfo - user data is passed in map format
	 * @return - String - jwt encrypted authorization token
	 * @throws JOSEException
	 */
	private String authTokenCreation(UserDTO user, Map<String, Object> userInfo) throws JOSEException {
		JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();
		claimsSet.issuer(Constants.TOKEN_ISSUER);
		claimsSet.subject(Constants.AUTH_TOKEN_SUBJECT);

		claimsSet.claim(Constants.USER_ID_PARAM, user.getId());
		claimsSet.claim(Constants.USER_DATA, userInfo);
		claimsSet.claim(Constants.APPLICATION_TYPE, Constants.WEB);

		claimsSet.expirationTime(
				Date.from(ZonedDateTime.now().plusMinutes(Constants.AUTH_TOKEN_EXPIRY_MINUTES).toInstant()));
		claimsSet.notBeforeTime(new Date());
		claimsSet.jwtID(UUID.randomUUID().toString());

		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet.build());
		RSAEncrypter encrypter = new RSAEncrypter(publicRsaKey);
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
	private String refreshTokenCreation(UserDTO user) throws JOSEException {
		JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder();
		claimsSet.issuer(Constants.TOKEN_ISSUER);
		claimsSet.subject(Constants.REFRESH_TOKEN_SUBJECT);
		claimsSet.claim(Constants.USER_ID_PARAM, user.getId());
		claimsSet.claim(Constants.APPLICATION_TYPE, Constants.WEB);
		claimsSet.expirationTime(
				Date.from(ZonedDateTime.now().plusMinutes(Constants.REFRESH_TOKEN_EXPIRY_HOURS).toInstant()));
		claimsSet.notBeforeTime(new Date());
		claimsSet.jwtID(UUID.randomUUID().toString());
		JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128GCM);
		EncryptedJWT jwt = new EncryptedJWT(header, claimsSet.build());
		RSAEncrypter encrypter = new RSAEncrypter(publicRsaKey);
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
	private void updateUserToken(long userId, String jwtToken, String jwtRefreshToken) {
		UserToken userToken = new UserToken();
		userToken.setUserId(userId);
		userToken.setAuthToken(jwtToken.substring(Constants.BEARER.length(), jwtToken.length()));
		userToken.setActive(true);
		userToken.setRefreshToken(jwtRefreshToken.substring(Constants.BEARER.length(), jwtRefreshToken.length()));
//		commonRepository.updateUserToken(userId, jwtToken, jwtRefreshToken);
		userTokenService.saveUserToken(userToken);
	}

}
