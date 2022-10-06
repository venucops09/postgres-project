package com.mdtlabs.coreplatform.spiceservice.mentalhealth.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.MentalHealthDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.MentalHealth;
import com.mdtlabs.coreplatform.spiceservice.mentalhealth.service.MentalHealthService;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;

import io.swagger.annotations.Api;

/**
 * This class is a controller class to perform operation on MentalHealth entity.
 * 
 * @author Karthick Murugesan
 * 
 */
@RestController
@RequestMapping(value = "/mentalhealth")
@Validated
@Api(basePath = "/mentalhealth", value = "master_data", description = "Mental health related APIs", produces = "application/json")
public class MentalHealthController {

	@Autowired
	MentalHealthService mentalHealthService;

	/**
	 * This method is used to add a new mentalhealth for a patient.
	 *
	 * @param mentalHealth
	 * @return MentalHealth Entity.
	 * @author Karthick Murugesan
	 */
	@RequestMapping(method = RequestMethod.POST)
	public SuccessResponse<MentalHealth> createMentalHealth(@RequestBody MentalHealth mentalHealth) {
		return new SuccessResponse<MentalHealth>(SuccessCode.MENTAL_HEALTH_SAVE,
				mentalHealthService.createOrUpdateMentalHealth(mentalHealth), HttpStatus.CREATED);
	}

	/**
	 * This method is used to get a mental health details of a patient.
	 *
	 * @param requestData
	 * @return MentalHealth Entity.
	 * @author Karthick Murugesan
	 */
	@RequestMapping(method = RequestMethod.GET)
	public SuccessResponse<MentalHealthDTO> getMentalHealthDetails(@RequestBody RequestDTO requestData) {
		return new SuccessResponse<MentalHealthDTO>(SuccessCode.MENTAL_HEALTH_SAVE,
				mentalHealthService.getMentalHealthDetails(requestData), HttpStatus.OK);
	}
}
