package com.mdtlabs.coreplatform.spiceservice.patientSymptom.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientSymptom;
import com.mdtlabs.coreplatform.spiceservice.patientSymptom.repository.PatientSymptomRepository;
import com.mdtlabs.coreplatform.spiceservice.patientSymptom.service.PatientSymptomService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * This class implements the PatientSymptomService interface and contains actual
 * business logic to perform operations on PatientSymptom entity.
 * 
 * @author Karthick Murugesan
 *
 */
@Service
public class PatientSymptomServiceImpl implements PatientSymptomService {

    @Autowired
    PatientSymptomRepository patientSymptomRepository;

    @Override
    public List<PatientSymptom> getSymptomsByPatientTracker(long patientTrackerId) {
        List<PatientSymptom> patientSymptomList = patientSymptomRepository.findByPatientTrackerIdOrderByUpdatedAtDesc(patientTrackerId);
        List<String> symptomList = null;
        // symptomList = patientSymptomList.stream().filter(patientSymptom -> patientSymptom.getSymptom() != null).map(patientSymptom -> patientSymptom.getSymptom().getSymptom()).collect(Collectors.toList());
        // List<String> otherSymptomList = patientSymptomList.stream().filter(patientSymptom -> patientSymptom.getSymptom() == null).map(patientSymptom -> patientSymptom.getOtherSymptom()).collect(Collectors.toList());
        // symptomList.addAll(otherSymptomList);
        return patientSymptomList;
    }


    public List<PatientSymptom> addPatientSymptoms(List<PatientSymptom> patientSymptoms) {
      
        if (patientSymptoms.isEmpty()) {
          throw new DataNotAcceptableException(11009);
        } 
        return patientSymptomRepository.saveAll(patientSymptoms);
    }
}
