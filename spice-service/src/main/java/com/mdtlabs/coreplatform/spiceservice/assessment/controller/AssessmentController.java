package com.mdtlabs.coreplatform.spiceservice.assessment.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.AssessmentDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.AssessmentResponseDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;
import com.mdtlabs.coreplatform.spiceservice.assessment.service.AssessmentService;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;

/**
 * This class is a controller class to perform operation on Assessment
 * operations.
 * 
 * @author Karthick Murugesan
 * 
 */
@RestController
@RequestMapping(value = "/assessment")
@Validated
public class AssessmentController {

	@Autowired
	private AssessmentService assessmentService;

	/**
	 * This method is used to add a new Assessment.
	 *
	 * @param assessmentDTO
	 * @return AssessmentDto Entity.
	 * @author Karthick Murugesan
	 */
	@PostMapping
	public SuccessResponse<AssessmentResponseDTO> createAssessment(@Valid @RequestBody AssessmentDTO assessmentDTO) {
		return new SuccessResponse<AssessmentResponseDTO>(SuccessCode.ASSESSMENT_SAVE,
				assessmentService.createAssessment(assessmentDTO), HttpStatus.CREATED);
	}

	/**
	 * This method is used to add a new BpLog Assessment
	 * 
	 * @param assessmentDTO
	 * @return Success Message
	 */
	@PostMapping("/bplog-create")
	public SuccessResponse<String> createBPLogAssessment(@Valid @RequestBody BpLog bpLog) {
		assessmentService.createAssessmentBpLog(bpLog);
		return new SuccessResponse<String>(SuccessCode.ASSESSMENT_BPLOG_SAVE, HttpStatus.CREATED);

	}

	/**
	 * This method is used to add a new Glucose Log Assessment
	 * 
	 * @param assessmentDTO
	 * @return
	 */
	@PostMapping("/glucoselog-create")
	public SuccessResponse<String> createGlucoseLogAssessment(@Valid @RequestBody GlucoseLog glucoseLog) {
		assessmentService.createAssessmentGlucoseLog(glucoseLog);
		return new SuccessResponse<String>(SuccessCode.ASSESSMENT_GLUCOSELOG_SAVE, HttpStatus.CREATED);

	}

}
