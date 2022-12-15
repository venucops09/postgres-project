package com.mdtlabs.coreplatform.userservice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.userservice.message.SuccessCode;
import com.mdtlabs.coreplatform.userservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.userservice.service.OrganizationService;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.model.dto.spice.OrganizationDTO;
import com.mdtlabs.coreplatform.common.model.entity.Organization;

/**
 * <p>
 * Organization Controller used to perform any action in the organization module like read and
 * write.
 * </p>
 * 
 * @author VigneshKumar created on Jan 30, 2022
 */
@RestController
@RequestMapping(value = "/organization")
public class OrganizationController {

	@Autowired
	private OrganizationService organizationService;

	/**
	 * <p>
	 * Add new organization for some action privileges.
	 * </p>
	 * 
	 * @param organization - organization information to be added
	 * @return Organization - response of adding the organization
	 */
	@PostMapping
	public SuccessResponse<Organization> addOrganization(@RequestBody Organization organization) {
		Organization newOrganization = organizationService.addOrganization(organization);
		return new SuccessResponse<>((null == newOrganization) ? SuccessCode.ORGANIZATION_SAVE_ERROR : SuccessCode.ORGANIZATION_SAVE, newOrganization, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Retrieve all the active organizations detail list.
	 * </p>
	 * 
	 * @return List<Organization> - List of Organization Entity
	 */
	@GetMapping
	public SuccessResponse<List<Organization>> getAllOrganizations() {
		List<Organization> organizationList = organizationService.getAllOrganizations();
		return new SuccessResponse((organizationList.isEmpty()) ? SuccessCode.GET_ORGANIZATIONS_ERROR : SuccessCode.GET_ORGANIZATIONS, organizationList, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Update Organization details like change the name.
	 * </p>
	 * 
	 * @param organization - organization information to be updated
	 * @return Organization - response of the updated organization
	 */
	@PutMapping
	public SuccessResponse<Organization> updateOrganization(@RequestBody Organization organization) {
		Organization updatedOrganization = organizationService.updateOrganization(organization);
		return new SuccessResponse<>((null == updatedOrganization) ? SuccessCode.ORGANIZATION_UPDATE_ERROR : SuccessCode.ORGANIZATION_UPDATE, updatedOrganization, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Inactive the organization by id.
	 * </p>
	 * 
	 * @param organizationId - the id of the organization which has be deleted
	 * @return Organization - response count of delete
	 */
	@DeleteMapping
	public SuccessResponse<Organization> deleteOrganizationById(@PathVariable(value = FieldConstants.ID) long organizationId) {
		int organization = organizationService.deleteOrganizationById(organizationId);
		return new SuccessResponse<>((organization == Constants.ONE) ? SuccessCode.ORGANIZATION_DELETE : SuccessCode.ORGANIZATION_DELETE_ERROR, organizationService.deleteOrganizationById(organizationId), HttpStatus.OK);
	}

	/**
	 * This method is used to get organization information by organization id
	 * 
	 * @param organizationId - the id of the organization
	 * @return Organization - organization information
	 */
	@GetMapping(value = "/{id}")
	public SuccessResponse<Organization> getOrganizationById(@PathVariable(value = FieldConstants.ID) long organizationId) {
		return new SuccessResponse<>(SuccessCode.GET_ORGANIZATION, organizationService.getOrganizationById(organizationId), HttpStatus.OK);
	}

	/**
	 * This method is used to get organization information by organization name
	 * 
	 * @param name - the name of the organization
	 * @return Organization - organization information
	 */
	@GetMapping(value = "/{name}")
	public SuccessResponse<Organization> getOrganizationByName(@PathVariable(value = FieldConstants.NAME) String name) {
		return new SuccessResponse<>(SuccessCode.GET_ORGANIZATION, organizationService.getOrganizationByName(name), HttpStatus.OK);
	}
	
	/**
	 * 
	 * Gets List of tenantIds of a user.
	 * @param id - user Id
	 * @return List<Long> - list of tenant IDs.
	 */
	@GetMapping("/get-user-tenants/{id}")
	public List<Long> getUserTenants(@PathVariable long id) {
		return organizationService.getUserTenants(id);
	}

	/**
	 * Creates an organization with users.
	 * 
	 * @param organizationDTO - Object with Organization details and List of users.
	 * @return Organization - Organization Entity.
	 * @author Niraimathi S
	 */
	@PostMapping("/create")
	public ResponseEntity<Organization> createOrganization(@RequestBody OrganizationDTO organizationDTO) {
		Organization newOrganization = organizationService.createOrganization(organizationDTO);
//		return new SuccessResponse<>((null == newOrganization) ? SuccessCode.ORGANIZATION_SAVE_ERROR : SuccessCode.ORGANIZATION_SAVE, newOrganization, HttpStatus.OK);
		return ResponseEntity.ok().body(newOrganization);
	}
	
	/**
	 * Gets child organization IDs of an organization.
	 * 
	 * @param tenantId organization tenantId
	 * @param formName organization form name
	 * @return Map<String, List<Long>> - collection of child organization IDs.
	 */
	@PostMapping("/get-child-organizations/{tenantId}")
	public Map<String, List<Long>> getChildOrganizations(@PathVariable("tenantId") Long tenantId,@RequestBody String formName) {
		return organizationService.getChildOrganizations(tenantId, formName);
	}
}
