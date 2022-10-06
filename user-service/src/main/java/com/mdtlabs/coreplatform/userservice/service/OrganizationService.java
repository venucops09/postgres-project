package com.mdtlabs.coreplatform.userservice.service;

import java.util.List;

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

}
