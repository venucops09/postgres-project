package com.mdtlabs.coreplatform.userservice.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.model.entity.Organization;
import com.mdtlabs.coreplatform.userservice.repository.OrganizationRepository;
import com.mdtlabs.coreplatform.userservice.service.OrganizationService;

/**
 * <p>
 * This service class contain all the business logic for organization module and perform
 * all the organization operation here.
 * </p>
 * 
 * @author VigneshKumar created on Jun 30, 2022
 */
@Service
public class OrganzationServiceImpl implements OrganizationService {

	@Autowired
	private OrganizationRepository organizationRepository;


	/**
	 * {@inheritDoc}
	 */
	public Organization addOrganization(Organization organization) {
		return organizationRepository.save(organization);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Organization> getAllOrganizations() {
		return organizationRepository.getAllOrganizations(Boolean.TRUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public Organization updateOrganization(Organization organization) {
		return organizationRepository.save(organization);
	}

	/**
	 * {@inheritDoc}
	 */
	@Modifying
	@Transactional
	public int deleteOrganizationById(long organizationId) {
		return organizationRepository.updateOrganizationStatusById(Boolean.FALSE, organizationId);
	}

	/**
	 * {@inheritDoc}
	 */
	public Organization getOrganizationById(long organizationId) {
		return organizationRepository.getOrganizationById(organizationId);
	}

	/**
	 * {@inheritDoc}
	 */
	public Organization getOrganizationByName(String name) {
		return organizationRepository.getOrganizationByName(name);
	}

}
