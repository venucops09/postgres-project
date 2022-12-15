package com.mdtlabs.coreplatform.spiceservice;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.mdtlabs.coreplatform.common.model.dto.UserDTO;
import com.mdtlabs.coreplatform.common.model.entity.User;

@FeignClient(name = "user")
public interface UserApiInterface {

	@GetMapping("")
	public UserDTO getLoggedInUser();

	@GetMapping("/organization/get-user-tenants/{id}")
	public List<Long> getUserTenants(@PathVariable long id);

	@GetMapping("/user/{id}")
	public UserDTO getPrescriberDetails(@RequestHeader("Authorization") String authToken, @PathVariable long id);

	@PostMapping("/user/get-by-tenants")
	public List<User> getUsersBasedOnOrgId(@RequestHeader("Authorization") String authToken,
			@RequestBody List<Long> orgIds);

}
