package com.project.telecounselor.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

import com.project.telecounselor.common.Constants;

import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * <p>
 * Authentication based operation performed in this service.
 * </p>
 * 
 * @author Vigneshkumar created on Jun 30, 2022.
 *
 */
@EnableDiscoveryClient
@EnableSwagger2WebMvc
@ComponentScan(value = Constants.PACKAGE_TELECOUNSELOR)
@EnableJpaRepositories(value = Constants.PACKAGE_TELECOUNSELOR)
@SpringBootApplication
public class AuthServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
