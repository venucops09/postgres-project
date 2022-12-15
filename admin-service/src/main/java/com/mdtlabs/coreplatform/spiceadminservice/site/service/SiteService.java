package com.mdtlabs.coreplatform.spiceadminservice.site.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.Site;

public interface SiteService {

	/**
	 * To add a site
	 * 
	 * @param site
	 * @return Site entity
	 * @author Jeyaharini T A
	 */
	public Site addSite(Site site);

	/**
	 * To update a site
	 * 
	 * @param site
	 * @return Site entity
	 * @author Jeyaharini T A
	 */
	public Site updateSite(Site site);

	/**
	 * To activate or deactive a site
	 * 
	 * @param id
	 * @param isActiveStatus
	 * @return Site entity
	 * @author Jeyaharini T A
	 */
	public Site activateDeactivateSite(long id, boolean isActiveStatus);

	/**
	 * Gets list of sites using tenantIds.
	 * 
	 * @param tenants List of tenantIds
	 * @return List of Site Entities.
	 * @author Niraimathi S
	 */
	public List<Site> getSitesByTenantIds(List<Long> tenants);

	/**
	 * Gets list if sites based onperating unit Id.
	 * 
	 * @param operatingUnitId operating unit Id.
	 * @return List of Site Entities.
	 */
	public List<Site> getSitesByOperatingUnitId(Long operatingUnitId);

	/**
	 * Gets a site using id and isDeleted fields
	 * 
	 * @param siteId site id
	 * @return Site entity
	 */
	public Site getSiteById(Long siteId);

}
