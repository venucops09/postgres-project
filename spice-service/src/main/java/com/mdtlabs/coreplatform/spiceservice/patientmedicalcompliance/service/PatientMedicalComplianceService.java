package com.mdtlabs.coreplatform.spiceservice.patientmedicalcompliance.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientMedicalCompliance;


/**
 * This is an interface to perform any actions in PatientMedicalComplianc related entities
 * 
 * @author Rajkumar
 *
 */
public interface PatientMedicalComplianceService {

    public List<PatientMedicalCompliance> addPatientMedicalCompliance(List<PatientMedicalCompliance> patientMedicalCompliance);
    
}

