package com.mdtlabs.coreplatform.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.mdtlabs.coreplatform.common.Constants;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * <p>
 * Authentication based operation performed in this service.
 * </p>
 * 
 * @author Rajkumar created on Jun 30, 2022.
 *
 */
@EnableDiscoveryClient
@EnableSwagger2WebMvc
@ComponentScan(value = Constants.PACKAGE_CORE_PLATFORM)
@EnableJpaRepositories(value = Constants.PACKAGE_CORE_PLATFORM)
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = Constants.AUTH_API, version = Constants.VERSION, description = Constants.DOCUMENTATION_AUTH_API))
public class AuthServerApplication {

	/**
	 * The main method of the application where the execution starts
	 * 
	 * @param args - string array of arguments is passed
	 */
	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}

}
