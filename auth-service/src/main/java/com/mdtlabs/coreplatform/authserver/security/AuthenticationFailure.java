package com.mdtlabs.coreplatform.authserver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdtlabs.coreplatform.common.Constants;

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

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		Map<String, String> responseBody = new HashMap<>();
		responseBody.put(Constants.MESSAGE, exception.getMessage());
		response.setContentType(Constants.CONTENT_TYPE);
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.getWriter().write(new ObjectMapper().writeValueAsString(responseBody));
	}
}
