package com.mdtlabs.coreplatform.spiceadminservice.site.service;

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

}
