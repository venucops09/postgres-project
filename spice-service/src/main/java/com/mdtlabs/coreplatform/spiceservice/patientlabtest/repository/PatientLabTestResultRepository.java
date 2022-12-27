package com.mdtlabs.coreplatform.spiceservice.patientlabtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientLabTestResult;

import java.util.List;

/**
 * This repository class is responsible for communication between database and server side.
 *
 * @author Rajkumar
 */
public interface PatientLabTestResultRepository extends JpaRepository<PatientLabTestResult, Long> {
    /**
     * This method is used for finding PatientLabTestResults based on PatientLabTestId, isDeleted and tenantId.
     * @param patientLabTestId PatientLabTest Id
     * @param isDeleted isDeleted field
     * @param tenantId tenantId
     * @return List of PatientLabTestResults.
     * @author Rajkumar
     */
    List<PatientLabTestResult> findAllByPatientLabTestIdAndIsDeletedAndTenantId(Long patientLabTestId,
                                                                                Boolean isDeleted, Long tenantId);
}
