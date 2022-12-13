package com.mdtlabs.coreplatform.servicediscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * <p>
 * In this class we need to check the status of the available service and used
 * to communicate between each of them.
 * </p>
 * 
 * @author Vigneshkumar created on 30, Jun 2022.
 */
@SpringBootApplication
@EnableEurekaServer
@EnableDiscoveryClient
public class ServiceDiscoveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceDiscoveryApplication.class, args);
	}
}
