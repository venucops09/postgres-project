package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Lifestyle;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * This interface is responsible for performing database operations between
 * server and Lifestyle entity.
 * 
 * @author Niraimathi S
 *
 */
@Repository
public interface LifestyleRepository extends GenericRepository<Lifestyle> {
	/**
	 * Gets list of Lifestyle entities based on isActive and isDeleted fields.
	 * 
	 * @return Lifestyle entities List
	 */
	List<Lifestyle> findByIsDeletedFalseAndIsActiveTrue();

}
