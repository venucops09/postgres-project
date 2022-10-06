package com.mdtlabs.coreplatform.spiceadminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

import com.mdtlabs.coreplatform.common.Constants;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@ComponentScan(value = Constants.PACKAGE_CORE_PLATFORM)
@EnableEurekaClient
//@EnableDiscoveryClient
@OpenAPIDefinition(info = @Info(title = "Admin API", version = Constants.VERSION, description = Constants.DOCUMENTATION_USER_API))
public class AdminServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AdminServiceApplication.class, args);
	}
}
