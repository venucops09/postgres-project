package com.mdtlabs.coreplatform.spiceservice.symptom.controller;


import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.entity.spice.Symptom;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceservice.symptom.service.SymptomService;

import java.util.List;

@RestController
@RequestMapping(value = "/symptom")
@Validated
@Api(basePath = "/symptom", value = "master_data", description = "Symptom related APIs", produces = "application/json")
public class SymptomController {

	@Autowired
	SymptomService symptomService;

	/**
	 * Gets all symptoms.
	 *
	 * @return List of Symptom entity
	 * @author Victor Jefferson
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public SuccessResponse<List<Symptom>> getAllSymptoms() {
		List<Symptom> symptomList = symptomService.getAllSymptoms();
		return new SuccessResponse<List<Symptom>>(SuccessCode.GET_SYMPTOMS, symptomList, HttpStatus.OK);
	}

	/**
	 * This method is used to add a new Symptom.
	 *
	 * @param symptom
	 * @return Symptom Entity.
	 * @author Victor Jefferson
	 */
	@RequestMapping(method = RequestMethod.POST)
	public SuccessResponse<Symptom> addSymptom(@RequestBody Symptom symptom) {
		return new SuccessResponse<Symptom>(SuccessCode.SYMPTOM_SAVE, symptomService.addSymptom(symptom),
				HttpStatus.CREATED);
	}
}