package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.DosageFrequency;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * This interface is responsible for performing database operations between
 * server and DosageFrequency entity.
 * 
 * @author Niraimathi S
 *
 */
@Repository
public interface DosageFrequencyRepository extends GenericRepository<DosageFrequency> {

}
