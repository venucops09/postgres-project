package com.mdtlabs.coreplatform.authserver.security;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.model.dto.AuthUserDTO;
import com.mdtlabs.coreplatform.common.service.UserTokenService;

public class LogoutSuccess implements LogoutHandler {

	@Autowired
	UserTokenService userTokenService;

	@Override
	public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

		AuthUserDTO authUserDTO = getLoggedInUser();
		if (!Objects.isNull(authUserDTO)) {

			String token = request.getHeader(HttpHeaders.AUTHORIZATION);

			if (!Objects.isNull(token)) {
				token = token.substring(Constants.BEARER.length(), token.length());
			}

			userTokenService.deleteUserTokenByToken(token, authUserDTO.getId());
		} else {

			System.out.println(" Session expired for the user to logout... ");

		}

//		super.onLogoutSuccess(request, response, authentication);
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
		return new ModelMapper().map(SecurityContextHolder.getContext().getAuthentication().getPrincipal(),
				AuthUserDTO.class);
	}

}
