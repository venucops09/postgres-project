package com.mdtlabs.coreplatform.spiceservice.bplog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientBpLogsDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;
import com.mdtlabs.coreplatform.spiceservice.bplog.service.BpLogService;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;

import io.swagger.annotations.Api;

import java.util.Map;

import javax.validation.*;

/**
 * This class is a controller class to perform operation on BpLog entity.
 * 
 * @author Karthick Murugesan
 * 
 */
@RestController
@RequestMapping(value = "/bplog")
@Validated
@Api(basePath = "/bplog", value = "master_data", description = "GlucoseLog related APIs", produces = "application/json")
public class BpLogController {

    @Autowired
    BpLogService bpLogService;
     
    /**
     * This method is used to add a new Bp log.
     *
     * @param bpLog
     * @return BpLog Entity.
     * @author Karthick Murugesan
     */
    @RequestMapping(method = RequestMethod.POST)
    public SuccessResponse<Map<String, Object>> addBpLog(@Valid @RequestBody BpLog bpLog) {
        BpLog bpLogResponse = bpLogService.addBpLog(bpLog, Constants.BOOLEAN_TRUE);
        return new SuccessResponse<Map<String, Object>>(
                SuccessCode.BP_LOG_SAVE,
                Map.of("avg_systolic", bpLogResponse.getAvgSystolic(), "avg_diastolic", bpLogResponse.getAvgDiastolic(), "bmi", bpLogResponse.getBmi()),
                HttpStatus.CREATED
        );
    }

    /**
     * This method is used to fetch bp logs using patient tracker id
     *
     * @param patientBpLogsRequestDto
     * @return PatientBpLogsDto Entity
     * @author Victor Jefferson
     */
    @RequestMapping(value = "/patient-bpLogsList", method = RequestMethod.POST)
    public SuccessResponse<PatientBpLogsDTO> getBpLogsByPatientTrackId(@RequestBody RequestDTO patientBpLogsRequestDto) {
        return new SuccessResponse<PatientBpLogsDTO>(
                SuccessCode.GET_BP_LOG_LIST,
                bpLogService.getPatientBPLogsWithSymptoms(patientBpLogsRequestDto),
                HttpStatus.OK
        );
    }
    
}
