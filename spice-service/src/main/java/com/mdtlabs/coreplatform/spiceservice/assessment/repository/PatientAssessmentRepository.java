package com.mdtlabs.coreplatform.spiceservice.assessment.repository;

import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientAssessment;
import com.mdtlabs.coreplatform.common.repository.TenantableRepository;


@Repository
public interface PatientAssessmentRepository extends TenantableRepository<PatientAssessment> {
    
}
