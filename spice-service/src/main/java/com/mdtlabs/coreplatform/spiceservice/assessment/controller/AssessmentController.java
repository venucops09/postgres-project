package com.mdtlabs.coreplatform.spiceservice.assessment.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.AssessmentDTO;
import com.mdtlabs.coreplatform.spiceservice.assessment.service.AssessmentService;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;

import io.swagger.annotations.Api;

import javax.validation.Valid;

/**
 * This class is a controller class to perform operation on Assessment operations.
 * 
 * @author Karthick Murugesan
 * 
 */
@RestController
@RequestMapping(value = "/assessment")
@Validated
@Api(basePath = "/assessment", value = "master_data", description = "Assessment related APIs", produces = "application/json")
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
    @RequestMapping(method = RequestMethod.POST)
    public SuccessResponse<AssessmentDTO> createAssessment(@Valid @RequestBody AssessmentDTO assessmentDTO) {
        return new SuccessResponse<AssessmentDTO>(
                SuccessCode.ASSESSMENT_SAVE,
                assessmentService.createAssessment(assessmentDTO),
                HttpStatus.CREATED
        );
    }  
}
