package com.mdtlabs.coreplatform.spiceservice.patientSymptom.service;


import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientSymptom;


/**
 * This is an interface to perform any actions in PatientSymptom related entities
 * 
 * @author Rajkumar
 *
 */
public interface PatientSymptomService {

    public List<PatientSymptom> getSymptomsByPatientTracker(long patientTrackerId);

    public List<PatientSymptom> addPatientSymptoms(List<PatientSymptom> patientSymptoms);
}
