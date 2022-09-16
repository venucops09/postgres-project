package com.project.telecounselor.authserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.telecounselor.common.Constants;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * <tt>AuthenticationFailure</tt> Extended SimpleUrlAuthenticationFailureHandler
 * to Send failure response.
 * </p>
 * 
 * @author Vigneshkumar created on 30 Jun 2022
 *
 */
public class AuthenticationFailure extends SimpleUrlAuthenticationFailureHandler {

	private static String CONTENT_TYPE = "application/json;charset=UTF-8";
	private static String MESSAGE = "message";

	/**
	 * To handle the business logic on authentication failure.
	 */
	@SuppressWarnings(Constants.SERIAL)
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		Map<String, String> responseBody = new HashMap<String, String>() {
			{
				put(MESSAGE, exception.getMessage());
			}
		};
		response.setContentType(CONTENT_TYPE);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));

	}

}
