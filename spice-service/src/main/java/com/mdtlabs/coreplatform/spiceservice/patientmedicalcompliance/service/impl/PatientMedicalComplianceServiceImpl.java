package com.mdtlabs.coreplatform.spiceservice.patientmedicalcompliance.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientMedicalCompliance;
import com.mdtlabs.coreplatform.spiceservice.patientmedicalcompliance.repository.PatientMedicalComplianceRepository;
import com.mdtlabs.coreplatform.spiceservice.patientmedicalcompliance.service.PatientMedicalComplianceService;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class PatientMedicalComplianceServiceImpl implements PatientMedicalComplianceService {

    @Autowired
    private PatientMedicalComplianceRepository patientMedicalComplianceRepository;

    public List<PatientMedicalCompliance> addPatientMedicalCompliance(List<PatientMedicalCompliance> patientMedicalCompliances) {
        if (Objects.isNull(patientMedicalCompliances)) {
            throw new BadRequestException(10008);
        }
        return patientMedicalComplianceRepository.saveAll(patientMedicalCompliances);
    }
    
}
