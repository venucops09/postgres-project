package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Diagnosis;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * This interface is responsible for performing database operations between
 * server and Diagnosis entity.
 * 
 * @author Rajkumar
 *
 */
@Repository
public interface DiagnosisRepository extends GenericRepository<Diagnosis> {

}
