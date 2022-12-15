package com.mdtlabs.coreplatform.userservice.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataConflictException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.dto.spice.OrganizationDTO;
import com.mdtlabs.coreplatform.common.model.entity.Organization;
import com.mdtlabs.coreplatform.userservice.repository.OrganizationRepository;
import com.mdtlabs.coreplatform.userservice.service.OrganizationService;
import com.mdtlabs.coreplatform.userservice.service.UserService;

/**
 * <p>
 * This service class contain all the business logic for organization module and
 * perform all the organization operation here.
 * </p>
 * 
 * @author VigneshKumar created on Jun 30, 2022
 */
@Service
public class OrganzationServiceImpl implements OrganizationService {

	@Autowired
	private OrganizationRepository organizationRepository;

	@Autowired
	private UserService userService;

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
		Organization existingOrganization = organizationRepository.findByIdAndIsDeletedFalse(organization.getId());
		if (Objects.isNull(existingOrganization)) {
			throw new DataNotFoundException(23008);
		}

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

	/**
	 * {@inheritDoc}
	 */
	public List<Long> getUserTenants(long userId) {
//		return organizationRepository.getUserTenants(userId);
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@org.springframework.transaction.annotation.Transactional
	public Organization createOrganization(OrganizationDTO organizationDTO) {
		if (Objects.isNull(organizationDTO)) {
			throw new BadRequestException(10000);
		}
		Organization existingOrganization = organizationRepository
				.findByNameIgnoreCaseAndIsDeletedFalse(organizationDTO.getOrganization().getName());

		if (!Objects.isNull(existingOrganization)) {
			throw new DataConflictException(23007);
		}
		Organization organization = organizationDTO.getOrganization();
		organization = organizationRepository.save(organization);
		userService.addOrganizationUsers(organizationDTO.getUsers(), organizationDTO.getRoles(),
				organizationDTO.isSiteOrganization());
		return organization;
	}

	/**
	 * {@inheritDoc}
	 */
	public Map<String, List<Long>> getChildOrganizations(long tenantId, String formName) {
		Map<String, List<Long>> childIds = new HashMap<>();
		List<Organization> childOrgs = new ArrayList<>();
		List<Long> childOrgIds = new ArrayList<>();
		List<Long> childOrgIdsToDelete = new ArrayList<>();

		if (formName.equalsIgnoreCase("country")) {
			childOrgs = organizationRepository.findByParentOrganizationId(tenantId);
			childOrgIds = childOrgs.stream().map(account -> account.getId()).collect(Collectors.toList());
			childIds.put("accountIds", childOrgIds);
			childOrgIdsToDelete.addAll(childOrgIds);
		}
		if (formName.equalsIgnoreCase("country") || formName.equalsIgnoreCase("account")) {
			if (formName.equalsIgnoreCase("account")) {
				childOrgs = organizationRepository.findByParentOrganizationId(tenantId);
			} else {
				childOrgs = organizationRepository.findByParentOrganizationIdIn(childOrgIds);
			}
			childOrgIds = childOrgs.stream().map(operatingUnit -> operatingUnit.getId()).collect(Collectors.toList());
			childIds.put("operatingUnitIds", childOrgIds);
			childOrgIdsToDelete.addAll(childOrgIds);
		}
		if (formName.equalsIgnoreCase("country") || formName.equalsIgnoreCase("account")
				|| formName.equalsIgnoreCase("operating unit")) {

			if (formName.equalsIgnoreCase("operating unit")) {
				childOrgs = organizationRepository.findByParentOrganizationId(tenantId);
			} else {
				childOrgs = organizationRepository.findByParentOrganizationIdIn(childOrgIds);
			}

			childOrgIds = childOrgs.stream().map(site -> site.getId()).collect(Collectors.toList());
			childIds.put("siteIds", childOrgIds);
			childOrgIdsToDelete.addAll(childOrgIds);
		}
		return childIds;
	}

}
