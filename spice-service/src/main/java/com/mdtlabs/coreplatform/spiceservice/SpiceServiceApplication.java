package com.mdtlabs.coreplatform.spiceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

import com.mdtlabs.coreplatform.common.Constants;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@ComponentScan(value = Constants.PACKAGE_CORE_PLATFORM)
@EnableEurekaClient
@EnableFeignClients
@EnableDiscoveryClient
@OpenAPIDefinition(info = @Info(title = "Spice API", version = Constants.VERSION, description = Constants.DOCUMENTATION_USER_API))
public class SpiceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpiceServiceApplication.class, args);
	}
}
