package com.mdtlabs.coreplatform.spiceservice.patientmedicalcompliance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientMedicalCompliance;


@Repository
public interface PatientMedicalComplianceRepository extends JpaRepository<PatientMedicalCompliance, Long> {
    
}
