package com.mdtlabs.coreplatform.spiceadminservice.site.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.Site;


@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

	public boolean existsByName(String name);
}
