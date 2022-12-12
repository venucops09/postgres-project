package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.MedicalCompliance;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * This interface is responsible for performing database operations between
 * server and MedicalCompliance entity.
 * 
 * @author Niraimathi S
 *
 */
@Repository
public interface MedicalComplianceRepository extends GenericRepository<MedicalCompliance> {

}
