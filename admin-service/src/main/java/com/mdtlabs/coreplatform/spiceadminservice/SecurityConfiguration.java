package com.mdtlabs.coreplatform.spiceadminservice;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * <p>
 *  Authentication has been done here for this service.
 * </p>
 * @author Vigneshkumar Created on 30 Jun 2022 
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration  extends WebSecurityConfigurerAdapter{
    @Override
    protected void configure(HttpSecurity http) throws Exception{
    	http.authorizeRequests()
		.antMatchers("/**").permitAll()
		.and().csrf().disable();
    }
}
