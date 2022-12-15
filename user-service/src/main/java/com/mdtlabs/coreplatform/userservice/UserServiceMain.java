package com.mdtlabs.coreplatform.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

import com.mdtlabs.coreplatform.common.Constants;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

/**
 * <p>
 * User based operation performed in this service.
 * </p>
 * 
 * @author Vigneshkumar created on Jun 30, 2022.
 */
@SpringBootApplication
@ComponentScan(value = Constants.PACKAGE_CORE_PLATFORM)
@EnableEurekaClient
@EnableFeignClients
@OpenAPIDefinition(info = @Info(title = Constants.USER_API, version = Constants.VERSION, description = Constants.DOCUMENTATION_USER_API))
public class UserServiceMain {

	/**
	 * This is the main method where the execution starts
	 * 
	 * @param args - argument array to be passed
	 */
	public static void main(String[] args) {
		SpringApplication.run(UserServiceMain.class, args);
	}

}
