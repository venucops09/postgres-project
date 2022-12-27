package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Unit;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * This interface is responsible for performing database operations between
 * server and Unit entity.
 * 
 * @author Rajkumar
 *
 */
@Repository
public interface UnitRepository extends GenericRepository<Unit> {

	/**
	 * Gets list of Units using Name
	 * 
	 * @param name unit name
	 * @return List of Unit entities
	 */
	List<Unit> findByNameNotLike(String name);

}
