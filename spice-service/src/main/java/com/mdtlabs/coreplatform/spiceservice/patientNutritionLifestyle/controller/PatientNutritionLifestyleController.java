package com.mdtlabs.coreplatform.spiceservice.patientNutritionLifestyle.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.PatientNutritionLifestyleRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientNutritionLifestyle;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceservice.patientNutritionLifestyle.service.PatientNutritionLifestyleService;

/**
 * This class is a controller class to perform operation on Patient Nutrition Lifestyle entity.
 *
 * @author Victor Jefferson
 *
 */
@RestController
@RequestMapping(value = "/patient-lifestyle")
@Validated
public class PatientNutritionLifestyleController {

    @Autowired
    PatientNutritionLifestyleService patientNutritionLifestyleService;

    /**
     * This method is used to add a new Patient Nutrition Lifestyle.
     *
     * @param patientNutritionLifestyleRequestDTO
     * @return PatientNutritionLifestyle Entity.
     * @author Victor Jefferson
     */
    @RequestMapping(method = RequestMethod.POST)
    public SuccessResponse<PatientNutritionLifestyle> addPatientNutritionLifestyle(@RequestBody PatientNutritionLifestyleRequestDTO patientNutritionLifestyleRequestDTO) {
        return new SuccessResponse<PatientNutritionLifestyle>(
                SuccessCode.PATIENT_NUTRITION_LIFESTYLE_SAVE,
                patientNutritionLifestyleService.addPatientNutritionLifestyle(patientNutritionLifestyleRequestDTO),
                HttpStatus.OK
        );
    }

    /**
     * This method is used to fetch Patient Nutrition Lifestyles using patient tracker id
     *
     * @param patientNutritionLifestyleRequestDTO
     * @return PatientNutritionLifestyles Entity
     * @author Victor Jefferson
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public SuccessResponse<List<PatientNutritionLifestyle>> getPatientNutritionLifeStyleList(@RequestBody PatientNutritionLifestyleRequestDTO patientNutritionLifestyleRequestDTO) {
        return new SuccessResponse<List<PatientNutritionLifestyle>>(
                SuccessCode.PATIENT_NUTRITION_LIFESTYLE_LIST,
                patientNutritionLifestyleService.getPatientNutritionLifeStyleList(patientNutritionLifestyleRequestDTO.getPatientTrackId()),
                HttpStatus.OK
        );
    }

    /**
     * This method is used to update Patient Nutrition Lifestyles.
     *
     * @param patientNutritionLifestyleRequestDTO
     * @return PatientNutritionLifestyle Entity.
     * @author Victor Jefferson
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public SuccessResponse<List<PatientNutritionLifestyle>> updatePatientNutritionLifestyle(@RequestBody PatientNutritionLifestyleRequestDTO patientNutritionLifestyleRequestDTO) {
        return new SuccessResponse<List<PatientNutritionLifestyle>>(
                SuccessCode.PATIENT_NUTRITION_LIFESTYLE_UPDATE,
                patientNutritionLifestyleService.updatePatientNutritionLifestyle(patientNutritionLifestyleRequestDTO),
                HttpStatus.OK
        );
    }

    /**
     * Used to Update Patient Nutrition Lifestyle by its Patient Track id and Patient Visit id.
     *
     * @param patientNutritionLifestyle
     * @return PatientNutritionLifestyle entity
     * @author Victor Jefferson
     */
    @RequestMapping(method = RequestMethod.POST, value = "/update-view-status")
    public SuccessResponse<PatientNutritionLifestyle> updatePatientNutritionLifestyleView(@RequestBody PatientNutritionLifestyle patientNutritionLifestyle) {
        return new SuccessResponse<PatientNutritionLifestyle>(SuccessCode.PATIENT_NUTRITION_LIFESTYLE_UPDATE_VIEW,
                patientNutritionLifestyleService.updatePatientNutritionLifestyleView(patientNutritionLifestyle), HttpStatus.OK);
    }

    /**
     * Used to delete a Patient Nutrition Lifestyle by its id.
     *
     * @param patientNutritionLifestyle
     * @return PatientNutritionLifestyle entity
     * @author Victor Jefferson
     */
    @RequestMapping(method = RequestMethod.POST, value = "/remove")
    public SuccessResponse<PatientNutritionLifestyle> deletePatientNutritionLifestyle(@RequestBody PatientNutritionLifestyle patientNutritionLifestyle) {
        return new SuccessResponse<PatientNutritionLifestyle>(SuccessCode.PATIENT_NUTRITION_LIFESTYLE_DELETE,
                patientNutritionLifestyleService.removePatientNutritionLifestyle(patientNutritionLifestyle), HttpStatus.OK);
    }
}
