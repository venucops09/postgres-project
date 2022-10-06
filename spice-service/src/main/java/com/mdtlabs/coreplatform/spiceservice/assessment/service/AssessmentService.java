package com.mdtlabs.coreplatform.spiceservice.assessment.service;

import com.mdtlabs.coreplatform.common.model.dto.spice.AssessmentDTO;

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
    public AssessmentDTO createAssessment(AssessmentDTO assessmentDTO);

    
}
