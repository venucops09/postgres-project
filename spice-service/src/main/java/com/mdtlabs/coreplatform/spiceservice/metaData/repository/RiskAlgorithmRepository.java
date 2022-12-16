package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.RiskAlgorithm;
import com.mdtlabs.coreplatform.common.repository.TenantableRepository;

/**
 * This interface is responsible for performing database operations between
 * server and RiksAlgorithm entity.
 * 
 * @author Niraimathi S
 *
 */
@Repository
public interface RiskAlgorithmRepository extends TenantableRepository<RiskAlgorithm> {

	/**
	 * Gets lsit of RiksAlgorithm by country id.
	 * 
	 * @param countryId country id
	 * @param sort      Sorting object
	 * @return List of RiskAlgorithm entities
	 */
	List<RiskAlgorithm> findByCountryId(Long countryId, Sort sort);

}
