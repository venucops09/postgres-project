package com.mdtlabs.coreplatform.spiceservice.patientNutritionLifestyle.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.exception.SpiceValidation;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientNutritionLifestyleRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientNutritionLifestyle;
import com.mdtlabs.coreplatform.spiceservice.common.repository.NutritionLifestyleRepository;
import com.mdtlabs.coreplatform.spiceservice.patientNutritionLifestyle.repository.PatientNutritionLifestyleRepository;
import com.mdtlabs.coreplatform.spiceservice.patientNutritionLifestyle.service.PatientNutritionLifestyleService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PatientNutritionLifestyleServiceImpl implements PatientNutritionLifestyleService {

    @Autowired
    PatientNutritionLifestyleRepository patientNutritionLifestyleRepository;
    
    @Autowired
    NutritionLifestyleRepository nutritionLifestyleRepository;

    /**
     * {@inheritDoc}
     */
    public PatientNutritionLifestyle addPatientNutritionLifestyle(PatientNutritionLifestyleRequestDTO patientNutritionLifestyleRequestDTO) {
        if (Objects.isNull(patientNutritionLifestyleRequestDTO.getPatientTrackId())) {
            throw new SpiceValidation(12006);
        } else {
            PatientNutritionLifestyle patientNutritionLifestyle = new PatientNutritionLifestyle();

            patientNutritionLifestyle.setPatientTrackId(patientNutritionLifestyleRequestDTO.getPatientTrackId());
            patientNutritionLifestyle.setPatientVisitId(patientNutritionLifestyleRequestDTO.getPatientVisitId());
            patientNutritionLifestyle.setTenantId(patientNutritionLifestyleRequestDTO.getTenantId());
            patientNutritionLifestyle.setLifestyles(nutritionLifestyleRepository.getNutritionLifestyleByIds(patientNutritionLifestyleRequestDTO.getLifestyle()));
            patientNutritionLifestyle.setClinicalNote(patientNutritionLifestyleRequestDTO.getClinicalNote());
            //referred by  params.user._id.toString(),
            // patientNutritionLifestyle.setReferredBy(patientNutritionLifestyleRequestDTO.getReferredBy());
            patientNutritionLifestyle.setReferredDate(patientNutritionLifestyleRequestDTO.getReferredDate());

            if (patientNutritionLifestyleRequestDTO.isNutritionist() == true) {
                //assessedby  params.user._id.toString(),
                patientNutritionLifestyle.setAssessedDate(new Date());
                patientNutritionLifestyle.setLifestyleAssessment(patientNutritionLifestyleRequestDTO.getLifestyleAssessment());
                patientNutritionLifestyle.setOtherNote(patientNutritionLifestyleRequestDTO.getOtherNote());
            }
            PatientNutritionLifestyle patientNutritionLifestyleResponse =  patientNutritionLifestyleRepository.save(patientNutritionLifestyle);
            return patientNutritionLifestyleResponse;
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<PatientNutritionLifestyle> getPatientNutritionLifeStyleList(long patientTrackId) {
        List<PatientNutritionLifestyle> patientNutritionLifestyles = patientNutritionLifestyleRepository.findByPatientTrackId(patientTrackId);
        return patientNutritionLifestyles;
    }

    /**
     * {@inheritDoc}
     */
    public List<PatientNutritionLifestyle> updatePatientNutritionLifestyle(PatientNutritionLifestyleRequestDTO patientNutritionLifestyleRequestDTO) {
        List<PatientNutritionLifestyle> updatedPatientNutritionLifestyles = new ArrayList<>();
        if (Objects.isNull(patientNutritionLifestyleRequestDTO)) {
            throw new SpiceValidation(12006);
        } else {
            List<Long> ids = patientNutritionLifestyleRequestDTO.getLifestyles().stream().map(
                    lifestyles->lifestyles.getId()
            ).collect(Collectors.toList());
            List<PatientNutritionLifestyle> patientNutritionLifestyles = patientNutritionLifestyleRepository.getPatientNutritionLifestyleByIds(ids);
            for (PatientNutritionLifestyle patientNutritionLifestyle : patientNutritionLifestyles) {
                PatientNutritionLifestyle singlePatientNutritionLifestyleFromRequest = patientNutritionLifestyleRequestDTO.getLifestyles().stream().filter(
                        lifestyles->lifestyles.getId() == patientNutritionLifestyle.getId()
                ).collect(Collectors.toList()).stream().findFirst().orElseThrow(() -> new SpiceValidation(28011));
                patientNutritionLifestyle.setLifestyleAssessment(singlePatientNutritionLifestyleFromRequest.getLifestyleAssessment());
                patientNutritionLifestyle.setOtherNote(singlePatientNutritionLifestyleFromRequest.getOtherNote());
                updatedPatientNutritionLifestyles.add(patientNutritionLifestyle);
            }
            List<PatientNutritionLifestyle> patientNutritionLifestylesResponse = patientNutritionLifestyleRepository.saveAll(updatedPatientNutritionLifestyles);
            return patientNutritionLifestylesResponse;
        }
    }


    /**
     * {@inheritDoc}
     */
    public boolean updatePatientNutritionLifestyleView(PatientNutritionLifestyle patientNutritionLifestyle) {
        if (Objects.isNull(patientNutritionLifestyle.getPatientTrackId()) || Objects.isNull(patientNutritionLifestyle.getPatientVisitId())) {
            throw new SpiceValidation(12006);
        }
        patientNutritionLifestyleRepository.updateByPatientTrackIdAndPatientVisitId(
                patientNutritionLifestyle.getPatientTrackId(),
                patientNutritionLifestyle.getPatientVisitId()
        );
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean removePatientNutritionLifestyle(PatientNutritionLifestyle patientNutritionLifestyle) {
        if (Objects.isNull(patientNutritionLifestyle.getId())) {
            throw new SpiceValidation(12006);
        }
        PatientNutritionLifestyle existingPatientNutritionLifestyle = (PatientNutritionLifestyle) patientNutritionLifestyleRepository.findById(patientNutritionLifestyle.getId()).get();
        existingPatientNutritionLifestyle.setDeleted(true);
        patientNutritionLifestyleRepository.save(existingPatientNutritionLifestyle);
        return true;
    }
}
