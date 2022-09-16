package com.project.telecounselor.authserver.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.project.telecounselor.common.Constants;

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
public class SecurityConfig extends WebSecurityConfigurerAdapter {

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
		source.registerCorsConfiguration(
				Constants.FORWARD_SLASH + Constants.ASTERISK_SYMBOL + Constants.ASTERISK_SYMBOL, config);
		return source;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		super.configure(web);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.cors().and().authorizeRequests()
				// Permit access to all for below urls
				.antMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
				.antMatchers(HttpMethod.GET, "/swagger-resources/**").permitAll()
				.antMatchers(HttpMethod.GET, "/v2/api-docs").permitAll()
				.antMatchers(HttpMethod.POST, "/customer/update-password/").permitAll()
				.antMatchers(HttpMethod.GET, "/customer/reset-password/**").permitAll()
				.antMatchers(HttpMethod.GET, "/webjars/springfox-swagger-ui/**").permitAll()
				.antMatchers(HttpMethod.GET, "/javainuse-openapi/**").permitAll()
				// Must be authenticated for other urls
				.anyRequest().authenticated().and().formLogin()
				// Login url and Permit access to all
				.loginProcessingUrl("/session").usernameParameter("username").passwordParameter("password")
				.successHandler(authenticationSuccess()).failureHandler(authenticationFailure()).and()
				.exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)).and()
				.logout().logoutUrl("/logout").deleteCookies("JSESSIONID").invalidateHttpSession(true)
				.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
				// Disable csrf
				.and().csrf().disable();
	}

}
