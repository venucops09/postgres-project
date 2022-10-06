package com.mdtlabs.coreplatform.spiceservice.patientNutritionLifestyle.service;


import java.util.List;

import com.mdtlabs.coreplatform.common.model.dto.spice.PatientNutritionLifestyleRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientNutritionLifestyle;

public interface PatientNutritionLifestyleService {

    /**
     * Creates a new Patient Nutrition Lifestyle
     *
     * @param
     * @return PatientNutritionLifestyle entity
     */
    public PatientNutritionLifestyle addPatientNutritionLifestyle(PatientNutritionLifestyleRequestDTO patientNutritionLifestyleRequestDTO);

    public List<PatientNutritionLifestyle> getPatientNutritionLifeStyleList(long id);

    public List<PatientNutritionLifestyle> updatePatientNutritionLifestyle(PatientNutritionLifestyleRequestDTO patientNutritionLifestyleRequestDTO);
    public boolean updatePatientNutritionLifestyleView(PatientNutritionLifestyle patientNutritionLifestyle);

    public boolean removePatientNutritionLifestyle(PatientNutritionLifestyle patientNutritionLifestyle);
}
