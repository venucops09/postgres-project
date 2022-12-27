package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Comorbidity;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * This interface is responsible for performing database operations between
 * server and Comorbidity entity.
 * 
 * @author Rajkumar
 *
 */
@Repository
public interface ComorbidityRepository extends GenericRepository<Comorbidity> {
	/**
	 * Gets list of comorbidities by isDeleted and isActive fields.
	 * 
	 * @return List of Comorbidity entity.
	 */
	List<Comorbidity> findByIsDeletedFalseAndIsActiveTrue();

}
