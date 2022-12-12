package com.mdtlabs.coreplatform.spiceadminservice.site.service.impl;

import java.util.List;
import java.util.Objects;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.exception.SpiceValidation;
import com.mdtlabs.coreplatform.common.model.entity.Site;
import com.mdtlabs.coreplatform.common.util.CommonUtil;
import com.mdtlabs.coreplatform.spiceadminservice.site.repository.SiteRepository;
import com.mdtlabs.coreplatform.spiceadminservice.site.service.SiteService;


@Service
public class SiteServiceImpl implements SiteService {

	@Autowired
	SiteRepository siteRepository;

	ModelMapper modelMapper = new ModelMapper();

	/**
	 * {@inheritDoc}
	 */
	public Site addSite(Site site) {

		if (Objects.isNull(site)) {
			throw new SpiceValidation(12006);
		}

		if (!Objects.isNull(site.getEmail()) && !CommonUtil.validateEmail(site.getEmail())) {
			throw new SpiceValidation(00001);
		}

		if (!Objects.isNull(site.getPhoneNumber()) && !CommonUtil.validatePhoneNumber(site.getPhoneNumber())) {
			throw new SpiceValidation(00002);
		}

		return siteRepository.save(site);
	}

	/**
	 * {@inheritDoc}
	 */
	public Site updateSite(Site site) {
		if (Objects.isNull(site)) {
			throw new SpiceValidation(12006);
		}
		if (!Objects.isNull(site.getEmail()) && !CommonUtil.validateEmail(site.getEmail())) {
			throw new SpiceValidation(00001);
		}
		if (!Objects.isNull(site.getPhoneNumber()) && !CommonUtil.validatePhoneNumber(site.getPhoneNumber())) {
			throw new SpiceValidation(00002);
		}
		Site existingSite = siteRepository.findById(site.getId()).get();
		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		modelMapper.map(site, existingSite);
		return siteRepository.save(existingSite);
	}

	/**
	 * {@inheritDoc}
	 */
	public Site activateDeactivateSite(long id, boolean isActiveStatus) {

		Site siteToUpdate = siteRepository.findById(id).get();
		if (Objects.isNull(siteToUpdate)) {
			throw new SpiceValidation(27007);
		}
		siteToUpdate.setActive(isActiveStatus);
		return siteRepository.save(siteToUpdate);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<Site> getSitesByTenantIds(List<Long> tenants) {
		return siteRepository.findByIsDeletedFalseAndTenantIdIn(tenants);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Site> getSitesByOperatingUnitId(Long operatingUnitId) {
		return siteRepository.findByOperatingUnitIdAndIsDeletedFalse(operatingUnitId);
	}

}
