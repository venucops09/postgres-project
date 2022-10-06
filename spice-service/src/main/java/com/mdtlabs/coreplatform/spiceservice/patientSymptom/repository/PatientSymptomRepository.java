package com.mdtlabs.coreplatform.spiceservice.patientSymptom.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientSymptom;

import java.util.List;

@Repository
public interface PatientSymptomRepository extends JpaRepository<PatientSymptom, Long> {


    /**
     * Get list of PatientSymptoms by patientTracker
     *
     * @param patientTracker
     * @return List of Symptom entity
     */
    public List<PatientSymptom> findByPatientTrackerIdOrderByUpdatedAtDesc(long patientTrackerId);
}
