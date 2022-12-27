package com.mdtlabs.coreplatform.spiceservice.patientvisit.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceservice.patientvisit.service.PatientVisitService;

/**
 * This class is a controller class to perform operation on PatientVisit entity.
 * 
 * @author Rajkumar
 * 
 */
@RestController
@RequestMapping(value = "/patientvisit")
@Validated
public class PatientVisitController {

	@Autowired
	private PatientVisitService patientVisitService;

	/**
	 * This method is used to add a new patientvisit.
	 *
	 * @param patientVisitDTO
	 * @return patientVisit Entity.
	 * @author Rajkumar
	 */
	@RequestMapping(method = RequestMethod.POST)
	public SuccessResponse<Map<String, Long>> addPatientVisit(@RequestBody RequestDTO patientVisitDTO) {
		return new SuccessResponse<Map<String, Long>>(SuccessCode.PATIENT_VISIT_SAVE,
				patientVisitService.addPatientVisit(patientVisitDTO), HttpStatus.CREATED);
	}

}
