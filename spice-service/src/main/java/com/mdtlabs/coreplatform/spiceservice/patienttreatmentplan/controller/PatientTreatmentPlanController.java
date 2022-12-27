package com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTreatmentPlan;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.service.PatientTreatmentPlanService;

import javax.validation.*;

@RestController
@RequestMapping(value = "/patient-treatment-plan")
public class PatientTreatmentPlanController {

    @Autowired
    PatientTreatmentPlanService treatmentPlanService;

    /**
     * This method is used to add a new treatment plan for the patient.
     * 
     * @param treatmentPlan
     * @return PatientTreatmentPlan Entity.
     */
    @RequestMapping(method = RequestMethod.POST)
    public SuccessResponse<PatientTreatmentPlan> addPatientTreatmentPlan(
            @RequestBody PatientTreatmentPlan treatmentPlan) {
        treatmentPlanService.addPatientTreatmentPlan(treatmentPlan);
        return new SuccessResponse<PatientTreatmentPlan>(SuccessCode.TREATMENTPLAN_SAVE, HttpStatus.CREATED);
    }

    /**
     * This method retrieves a patient treatment plan.
     * 
     * @param request
     * @return PatientTreatmentPlan Entity
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public SuccessResponse<PatientTreatmentPlan> getPatientTreatmentPlan(@RequestBody RequestDTO request) {
        return new SuccessResponse<PatientTreatmentPlan>(SuccessCode.GET_TREATMENTPLAN,
                treatmentPlanService.getPatientTreatmentPlanDetails(request.getPatientTrackId(), request.getTenantId()), HttpStatus.OK);
    }

    /**
	 * Used to soft delete a labtest.
	 * 
	 * @param patientTreatmentPlan
	 * @return Boolean
	 * @author Rajkumar
	 */
	@RequestMapping(value = "/update", method = RequestMethod.PUT)
	public SuccessResponse<Boolean> updateTreatmentPlanData(@Valid @RequestBody PatientTreatmentPlan patientTreatmentPlan) {
        treatmentPlanService.updateTreatmentPlanData(patientTreatmentPlan);
        return new SuccessResponse<Boolean>(SuccessCode.TREATMENTPLAN_UPDATE, HttpStatus.OK);
	}
}
