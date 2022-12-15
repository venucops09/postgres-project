package com.mdtlabs.coreplatform.spiceservice.assessment.service;

import com.mdtlabs.coreplatform.common.model.dto.spice.AssessmentDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.AssessmentResponseDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;

/**
 * This is an interface to perform any actions in assessment related entities
 * 
 * @author Karthick Murugesan
 *
 */
public interface AssessmentService {

	/**
	 * Creats a new assessment based on AssessmentDTO
	 * 
	 * @param assessmentDTO Object with patient assessment data.
	 * @return assessmentDTO
	 */
	public AssessmentResponseDTO createAssessment(AssessmentDTO assessmentDTO);

	/**
	 * Creates a new assessment BP Log
	 * 
	 * @param BpLog
	 */
	public void createAssessmentBpLog(BpLog bpLog);

	/**
	 * Create a new assessment Glucose Log
	 * 
	 * @param GlucoseLog
	 * @return
	 */
	public GlucoseLog createAssessmentGlucoseLog(GlucoseLog glucoseLog);

}
