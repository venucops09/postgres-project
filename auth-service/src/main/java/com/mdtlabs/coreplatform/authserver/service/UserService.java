package com.mdtlabs.coreplatform.authserver.service;

import java.text.ParseException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface UserService {

	/**
	 * To generate a new refresh token
	 * 
	 * @param userId
	 * @param authToken
	 * @return Map of auth token and refresh token
	 * @throws ParseException
	 * @throws JsonMappingException
	 * @throws JsonProcessingException
	 * 
	 * @author Rajkumar
	 */
	Map<String, String> generateRefreshToken(long userId, String authToken)
			throws ParseException, JsonMappingException, JsonProcessingException;

}
