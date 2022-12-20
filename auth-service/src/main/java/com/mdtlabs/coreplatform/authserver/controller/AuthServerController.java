package com.mdtlabs.coreplatform.authserver.controller;

import java.text.ParseException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.mdtlabs.coreplatform.authserver.service.UserService;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.FieldConstants;

@RestController
@RequestMapping(value = "/auth")
public class AuthServerController {

	@Autowired
	UserService userService;

	/**
	 * To generate new refresh token
	 * 
	 * @param requestBody
	 * @param response
	 * @return
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 * @throws ParseException
	 */
	@PostMapping("/generate-token")
	public Map<String, String> generateRefreshToken(@RequestBody Map<String, Object> requestBody)
			throws JsonMappingException, JsonProcessingException, ParseException {
		String refreshToken = String.valueOf(requestBody.get(Constants.REFRESH_TOKEN));
		long userId = Long.parseLong(requestBody.get(FieldConstants.USER_ID).toString());
		System.out.println("refresh token: " + refreshToken);
		System.out.println("userId: " + userId);
		Map<String, String> tokensMap = userService.generateRefreshToken(userId, refreshToken);

		if (!tokensMap.isEmpty()) {
//			response.setHeader(Constants.AUTHORIZATION, tokensMap.get("authToken"));
//			response.setHeader(Constants.REFRESH_TOKEN, tokensMap.get("refreshToken"));
		}

		return tokensMap;
	}

}
