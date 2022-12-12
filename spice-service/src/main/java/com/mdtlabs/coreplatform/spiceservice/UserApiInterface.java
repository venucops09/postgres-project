package com.mdtlabs.coreplatform.spiceservice;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.mdtlabs.coreplatform.common.model.dto.UserDTO;

@FeignClient(name = "user", path = "/user-service")
public interface UserApiInterface {

	@GetMapping("")
	public UserDTO getLoggedInUser();

	@GetMapping("/organization/get-user-tenants/{id}")
	public List<Long> getUserTenants(@PathVariable long id);

}
