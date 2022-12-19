package com.mdtlabs.coreplatform.userservice.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mdtlabs.coreplatform.common.model.dto.spice.OrganizationDTO;
import com.mdtlabs.coreplatform.common.model.entity.Organization;


/**
 * <p>
 * This an interface class for organization module you can implemented this class in any
 * class.
 * </p>
 * 
 * @author VigneshKumar created on Jun 30, 2022
 */
public interface OrganizationService {

	/**
	 * <p>
	 * This method used to add a organization detail.
	 * </p>
	 * 
	 * @param organization - organization to be added
	 * @return Organization - added organization information
	 */
	Organization addOrganization(Organization organization);

	/**
	 * <p>
	 * This method used to retrieve all the active organization.
	 * </p>
	 * 
	 * @return List<Organization> - list of Organization Entity
	 */
	List<Organization> getAllOrganizations();

	/**
	 * <p>
	 * This method used to update a organization details.
	 * </p>
	 * 
	 * @param organization - updating organization
	 * @return Organization - update organization entity
	 */
	Organization updateOrganization(Organization organization);

	/**
	 * <p>
	 * This method used to inactive a organization using id.
	 * </p>
	 * 
	 * @param organizationId - organization id to be deleted
	 * @return int - value indicating the organization delete
	 */
	int deleteOrganizationById(long organizationId);

	/**
	 * <p>
	 * This method used to get a organization detail by id.
	 * </p>
	 * 
	 * @param organizationId - organization id
	 * @return Organization - organization entity
	 */
	Organization getOrganizationById(long organizationId);

	/**
	 * <p>
	 * This method used to get a organization detail by name.
	 * </p>
	 * 
	 * @param name - organization name
	 * @return Organization - organization entity
	 */
	Organization getOrganizationByName(String name);
	
	/**
	 * Gets user tenant Ids using user Id.
	 * 
	 * @param userId - user ID
	 * @return List<Long> - List of user tenantIds.
	 */
	List<Long> getUserTenants(long userId);

	/**
	 * Creates an organization with users.
	 * 
	 * @param organization - organization details with users
	 * @return Organization - organization entity.
	 */
	Organization createOrganization(OrganizationDTO organization);

	/**
	 * Gets child organization IDs of an organization.
	 * 
	 * @param tenantId organization tenantId
	 * @param formName organization form name
	 * @return Map<String, List<Long>> - collection of child organization IDs.
	 */
	Map<String, List<Long>> getChildOrganizations(long tenantId, String formName);
	
	/**
	 * To get list of organizations based on list of ids.
	 * 
	 * @param roles - list of organization ids
	 * @return Set<Role> - List of organization entity
	 */
	Set<Organization> getOrganizationsByIds(List<Long> organizationIds);


}
