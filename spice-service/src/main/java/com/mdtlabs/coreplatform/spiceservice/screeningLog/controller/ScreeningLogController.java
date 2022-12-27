package com.mdtlabs.coreplatform.spiceservice.screeningLog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.ScreeningLogDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.ScreeningLog;
import com.mdtlabs.coreplatform.common.util.ScreeningInfo;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceservice.screeningLog.service.ScreeningLogService;

/**
 * This class is a controller class to perform operation on PatientVisit entity.
 *
 * @author Rajkumar
 */
@RestController
@RequestMapping(value = "/screeninglog")
@Validated
public class ScreeningLogController {

	@Autowired
	private ScreeningLogService screeningLogService;

	/**
	 * This method is used to add a new screening log.
	 *
	 * @param screeningLog
	 * @return ScreeningLog Entity.
	 * @author Rajkumar
	 */
	@RequestMapping(method = RequestMethod.POST)
	public SuccessResponse<ScreeningLog> addScreeningLog(
			@Validated(ScreeningInfo.class) @RequestBody ScreeningLogDTO screeningLog) {
		screeningLogService.addScreeningLog(screeningLog);
		return new SuccessResponse<ScreeningLog>(SuccessCode.SCREENING_LOG_SAVE, HttpStatus.CREATED);
	}

    /**
     * This method is used to retrieve single screening log using
     * glucoseLogId
     *
     * @param screeningRequestDto
     * @return ScreeningLog Entity
     * @author Victor Jefferson
     */
    @RequestMapping(value = "/details", method = RequestMethod.POST)
    public SuccessResponse<ScreeningLog> getScreeningLogById(
            @RequestBody RequestDTO requestDto) {
        return new SuccessResponse<ScreeningLog>(
                SuccessCode.GET_SCREENING_LOG,
                screeningLogService.getScreeningDetails(requestDto),
                HttpStatus.OK);
                }
}
