package com.mdtlabs.coreplatform.spiceadminservice;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.mdtlabs.coreplatform.common.model.dto.spice.OrganizationDTO;
import com.mdtlabs.coreplatform.common.model.entity.Organization;
import com.mdtlabs.coreplatform.common.model.entity.User;

/**
 * This interface is used to access User service APIs.
 * 
 * @author Rajkumar
 *
 */
@FeignClient(name = "user-service")
public interface UserApiInterface {
	@PostMapping("/organization/create")
	public ResponseEntity<Organization> createOrganization(@RequestHeader("Authorization") String token,
			@RequestHeader("TenantId") long tenantId, @RequestBody OrganizationDTO organizationDTO);

	@PostMapping("/organization/get-child-organizations/{tenantId}")
	public Map<String, List<Long>> getChildOrganizations(@RequestHeader("Authorization") String token,
			@RequestHeader("TenantId") long tenantId, @PathVariable("tenantId") Long tenantID,
			@RequestBody String formName);

	@PostMapping("/user/get-by-tenants")
	public List<User> getUsersByTenantIds(@RequestHeader("Authorization") String token,
			@RequestHeader("TenantId") long tenantId, @RequestBody List<Long> tenantIds);
}
