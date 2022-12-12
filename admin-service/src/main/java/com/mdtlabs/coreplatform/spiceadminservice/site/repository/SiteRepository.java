package com.mdtlabs.coreplatform.spiceadminservice.site.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.Site;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {
	
	public boolean existsByName(String name);

	/**
	 * Gets list of sites based on list of tenant Ids.
	 * 
	 * @param tenants list of tenant IDs
	 * @return List of site entities
	 */
	public List<Site> findByIsDeletedFalseAndTenantIdIn(List<Long> tenants);

	
	/**
	 * Gets list of sites based on oeprating unit Id.
	 * 
	 * @param operatingUnitId Operating unit ID
	 * @return List of site entities
	 */
	public List<Site> findByOperatingUnitIdAndIsDeletedFalse(Long operatingUnitId);
}
