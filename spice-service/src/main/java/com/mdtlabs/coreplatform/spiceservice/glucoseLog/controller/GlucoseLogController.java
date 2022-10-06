package com.mdtlabs.coreplatform.spiceservice.glucoseLog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientGlucoseLogDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;
import com.mdtlabs.coreplatform.spiceservice.glucoseLog.service.GlucoseLogService;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;

import io.swagger.annotations.Api;

import javax.validation.*;


/**
 * This class is a controller class to perform operation on GlucoseLog entity.
 * 
 * @author Karthick Murugesan
 * 
 */
@RestController
@RequestMapping(value = "/glucoselog")
@Validated
@Api(basePath = "/glucoselog", value = "master_data", description = "GlucoseLog related APIs", produces = "application/json")
public class GlucoseLogController {

    @Autowired
    private GlucoseLogService glucoseLogService;

    /**
     * This method is used to add a new Glucose log.
     *
     * @param glucoseLog
     * @return GlucoseLog Entity.
     * @author Victor Jefferson
     */
    @RequestMapping(method = RequestMethod.POST)
    public SuccessResponse<GlucoseLog> addGlucoseLog(@RequestBody GlucoseLog glucoseLog) {
        
        glucoseLogService.addGlucoseLog(glucoseLog, Constants.BOOLEAN_TRUE);
        return new SuccessResponse<GlucoseLog>(SuccessCode.GLUCOSE_LOG_SAVE, HttpStatus.CREATED);
    }

    /**
     * This method is used to retrieve single Glucose log using
     * glucoseLogId
     *
     * @param glucoseLogId
     * @return GlucoseLog Entity
     * @author Victor Jefferson
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public SuccessResponse<GlucoseLog> getGlucoseLogById(@PathVariable(value = FieldConstants.ID) long glucoseLogId) {
        return new SuccessResponse<GlucoseLog>(
                SuccessCode.GET_GLUCOSE_LOG,
                glucoseLogService.getGlucoseLogById(glucoseLogId),
                HttpStatus.OK
        );
    }

    /**
     * This method is used to fetch bp logs using patient tracker id
     *
     * @param patientBpLogsRequestDto
     * @return PatientBpLogsDto Entity
     * @author Victor Jefferson
     */
    @RequestMapping(value = "/patient-glucoseLogsList", method = RequestMethod.POST)
    public SuccessResponse<PatientGlucoseLogDTO> getBpLogsByPatientTrackId(@RequestBody RequestDTO patientBpLogsRequestDto) {
        return new SuccessResponse<PatientGlucoseLogDTO>(
                SuccessCode.GET_BP_LOG_LIST,
                glucoseLogService.getPatientGlucoseLogsWithSymptoms(patientBpLogsRequestDto),
                HttpStatus.OK
        );
    }
}
