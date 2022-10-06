package com.mdtlabs.coreplatform.spiceservice.patientTracker.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.model.dto.spice.ConfirmDiagnosisDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchPatientListDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;

import io.swagger.annotations.Api;

/**
 * This class is a controller class to perform operation on PatientTracker entity.
 *
 * @author Karthick Murugesan
 */
@RestController
@RequestMapping(value = "/patienttracker")
@Validated
@Api(basePath = "/patientTracker", value = "master_data", description = "Patient tracker related APIs",
        produces = "application/json")
public class PatientTrackerController {

    @Autowired
    PatientTrackerService patientTrackerService;

    /**
     * This method is used to add a new patient tracker.
     *
     * @param patientTracker
     * @return PatientTracker Entity.
     * @author Victor Jefferson
     */
    @RequestMapping(method = RequestMethod.POST)
    public SuccessResponse<PatientTracker> addPatientTracker(@RequestBody PatientTracker patientTracker) {
		patientTrackerService.addOrUpdatePatientTracker(patientTracker);
		return new SuccessResponse<PatientTracker>(SuccessCode.PATIENT_TRACKER_SAVE, HttpStatus.CREATED);
    }


    /**
     * This method is used to retrieve single patient tracker using
     * patientTrackerId.
     *
     * @param patientTrackerId
     * @return PatientTracker Entity
     * @author Victor Jefferson
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public SuccessResponse<PatientTracker> getPatientTrackerById(
            @PathVariable(value = FieldConstants.ID) long patientTrackerId) {
        return new SuccessResponse<PatientTracker>(SuccessCode.GET_PATIENT_TRACKER,
                patientTrackerService.getPatientTrackerById(patientTrackerId), HttpStatus.OK);
    }

	/**
	 * This method is used to retrieve patient details with filters like firstname,
	 * lastname, medical review date etc.,
	 *
	 * @param patientRequestDTO
	 * @return List of patient tracker entity
	 * @author Jeyaharini T A
	 */
	@GetMapping("/search")
	public SuccessResponse<List<SearchPatientListDTO>> searchPatients(
			@RequestBody PatientRequestDTO patientRequestDTO) {
		return new SuccessResponse<List<SearchPatientListDTO>>(SuccessCode.SEARCH_PATIENTS,
				patientTrackerService.searchPatients(patientRequestDTO), HttpStatus.OK);
	}

	/**
	 * This method is used to Update confirm diagnosis details to the patient.
	 *
	 * @param confirmDiagnosis
	 * @return ConfirmDiagnosisDTO
	 * @author Karthick Murugesan
	 */
	@RequestMapping(value = "/confirm-diagnosis/update", method = RequestMethod.PATCH)
	public SuccessResponse<ConfirmDiagnosisDTO> updateConfirmDiagnosis(
			@RequestBody ConfirmDiagnosisDTO confirmDiagnosis) {
		return new SuccessResponse<ConfirmDiagnosisDTO>(SuccessCode.SEARCH_PATIENTS,
				patientTrackerService.updateConfirmDiagnosis(confirmDiagnosis), HttpStatus.OK);
	}

}
