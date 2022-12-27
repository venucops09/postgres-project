package com.mdtlabs.coreplatform.userservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mdtlabs.coreplatform.AuthenticationFilter;

/**
 * <p>
 * Authentication has been done here for this service.
 * </p>
 * 
 * @author Rajkumar Created on 30 Jun 2022
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Autowired
	private final AuthenticationFilter authenticationFilter;

	public SecurityConfiguration(AuthenticationFilter authenticationFilter) {
		this.authenticationFilter = authenticationFilter;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
				.antMatchers(HttpMethod.GET, "/swagger-resources/**").permitAll()
				.antMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
				.antMatchers(HttpMethod.GET, "/webjars/swagger-ui/**").permitAll().anyRequest()
				.authenticated().and()
				.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class).exceptionHandling()
				.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)).and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.csrf().disable();
		return http.build();
	}

}
