package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Complication;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * This interface is responsible for performing database operations between
 * server and Complication entity.
 * 
 * @author Rajkumar
 *
 */
@Repository
public interface ComplicationRepository extends GenericRepository<Complication> {
	/**
	 * Gets Complication entity list by isDeleted and isActive fields
	 * 
	 * @return List of Complication entities.
	 */
	List<Complication> findByIsDeletedFalseAndIsActiveTrue();

}
