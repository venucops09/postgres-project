package com.mdtlabs.coreplatform.authserver.swagger;

import java.util.List;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.mdtlabs.coreplatform.common.Constants;

/**
 * <p>
 * Swagger to describe what are endpoints are present in this service with their
 * reference request and response.
 * </p>
 * 
 * @author Rajkumar Created on 30 Jun 2022
 *
 */
@Component
@EnableAutoConfiguration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = Constants.DOCUMENTATION_SWAGGER)
public class SwaggerServicesConfig {

	List<SwaggerServices> swagger;

	public List<SwaggerServices> getServices() {
		return swagger;
	}

	public void setServices(List<SwaggerServices> swaggerResources) {
		this.swagger = swaggerResources;
	}

	@EnableConfigurationProperties
	@ConfigurationProperties(prefix = Constants.DOCUMENTATION_SWAGGER_SERVICES)
	public static class SwaggerServices {
		private String name;
		private String url;
		private String version;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

	}

}