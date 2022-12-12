package com.mdtlabs.coreplatform.spiceservice.assessment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientAssessment;


@Repository
public interface PatientAssessmentRepository extends JpaRepository<PatientAssessment, Long> {
    
}
