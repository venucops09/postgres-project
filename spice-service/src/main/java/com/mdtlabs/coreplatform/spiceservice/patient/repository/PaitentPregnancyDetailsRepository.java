package com.mdtlabs.coreplatform.spiceservice.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientPregnancyDetails;

/**
 * This interface handles the connection between service layer and database and is responsible for database operations
 * for PatientPregnancyDetails entity.
 *
 * @author Rajkumar
 */
public interface PaitentPregnancyDetailsRepository extends JpaRepository<PatientPregnancyDetails, Long> {
    /**
     * This method gets the entity using patientTrackId and isDeleted.
     *
     * @param patientTrackId PatientTrack Id
     * @param isDeleted      isDeleted field
     * @return PatientPregnancyDetails Entity.
     * @author Rajkumar
     */
    PatientPregnancyDetails findByPatientTrackIdAndIsDeleted(Long patientTrackId, Boolean isDeleted);

    /**
     * Finds PatientPregnancyDetails by its id and isDeleted field.
     *
     * @param patientPregnancyId patientPregnancyId
     * @param isDeleted          isDeleted field.
     * @return PatientPregnancyDetails Entity.
     * @author Rajkumar
     */
    PatientPregnancyDetails findByIdAndIsDeleted(Long patientPregnancyId, Boolean isDeleted);
}
