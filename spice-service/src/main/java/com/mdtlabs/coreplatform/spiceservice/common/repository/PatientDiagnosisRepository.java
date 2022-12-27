package com.mdtlabs.coreplatform.spiceservice.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientDiagnosis;


/**
 * 
 * @author Rajkumar
 *
 */

@Repository
public interface PatientDiagnosisRepository extends JpaRepository<PatientDiagnosis, Long> {

	/**
	 * This methods finds a patientDiagnosis by patientTrackId, isActive and isDeleted Fields.
	 * 
	 * @param id
	 * @param isActive
	 * @param isDeletd
	 * @return PatientDiagnosis Entity.
	 */
	PatientDiagnosis findByPatientTrackIdAndIsActiveAndIsDeleted(Long patientTrackId, Boolean isActive, Boolean isDeleted);

}