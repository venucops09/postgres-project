package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.DosageForm;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * This interface is responsible for performing database operations between
 * server and DosageForm entity.
 * 
 * @author Rajkumar
 *
 */
@Repository
public interface DosageFormRepository extends GenericRepository<DosageForm> {

	/**
	 * Gets list of Dosage form entities except "Other" dosageform
	 * 
	 * @param name dosage form name
	 * @return List of DosageForm entities
	 */
	List<DosageForm> findByNameNotLike(String name);

}
