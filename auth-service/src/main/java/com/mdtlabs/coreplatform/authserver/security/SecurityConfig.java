package com.mdtlabs.coreplatform.authserver.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.FieldConstants;

/**
 * <p>
 * In this class is the entry point for user authentication and authorization.
 * </p>
 * 
 * @author Vigneshkumar Created on 30 Jun 2022
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationSuccess authenticationSuccess() {
		return new AuthenticationSuccess();
	}

	@Bean
	public AuthenticationFailure authenticationFailure() {
		return new AuthenticationFailure();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		return new AuthenticationProvider();
	}

	@Bean
	public LogoutSuccess logoutSuccess() {
		return new LogoutSuccess();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration config = new CorsConfiguration();
		config.setAllowCredentials(Boolean.TRUE);
		config.addAllowedOriginPattern(Constants.ASTERISK_SYMBOL);
		config.addAllowedHeader(Constants.ASTERISK_SYMBOL);
		config.addAllowedMethod(HttpMethod.HEAD);
		config.addAllowedMethod(HttpMethod.GET);
		config.addAllowedMethod(HttpMethod.PUT);
		config.addAllowedMethod(HttpMethod.POST);
		config.addAllowedMethod(HttpMethod.DELETE);
		config.addAllowedMethod(HttpMethod.PATCH);
		config.addAllowedMethod(HttpMethod.OPTIONS);
//		config.addExposedHeader(FieldConstants.REFRESH_TOKEN);
		source.registerCorsConfiguration(
				Constants.FORWARD_SLASH + Constants.ASTERISK_SYMBOL + Constants.ASTERISK_SYMBOL, config);
		return source;
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring().antMatchers("/images/**", "/js/**", "/webjars/**");
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception { 
		http.cors().and().authorizeRequests()
				.antMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
				.anyRequest().authenticated().and()
				.formLogin().loginProcessingUrl("/session")
				.usernameParameter(FieldConstants.USERNAME).passwordParameter(FieldConstants.PASSWORD)
				.successHandler(authenticationSuccess())
				.failureHandler(authenticationFailure()).and()
				.exceptionHandling()
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
				.and().logout().logoutUrl("/logout")
				.deleteCookies("JSESSIONID").invalidateHttpSession(Boolean.TRUE)
				.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()).and()
				.csrf().disable();
		return http.build();
	}

}
