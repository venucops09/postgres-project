package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.CurrentMedication;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * This interface is responsible for performing database operations between
 * server and CurrentMedication entity.
 * 
 * @author Rajkumar
 *
 */
@Repository
public interface CurrentMedicationRespository extends GenericRepository<CurrentMedication> {
	/**
	 * Gets CurrentMedication entity list based on isActive And isDeleted fields.
	 * 
	 * @return List of CurrentMedication entities
	 */
	List<CurrentMedication> findByIsDeletedFalseAndIsActiveTrue();

}
