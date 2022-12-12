package com.mdtlabs.coreplatform.spiceservice.staticdata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.MedicalReviewStaticDataDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.StaticDataDTO;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceservice.staticdata.service.StaticDataService;

@RestController
@RequestMapping(value = "/static-data")
public class StaticDataController {

	@Autowired
	StaticDataService staticDataService;

	/**
	 * To get common static data.
	 * 
	 * @return StaticDataDTO containing required data.
	 */
	@GetMapping()
	public SuccessResponse<StaticDataDTO> getStaticData() {
		return new SuccessResponse<>(SuccessCode.GET_STATIC_DATA, staticDataService.getStaticData(), HttpStatus.OK);
	}

	/**
	 * To get Medical review related static data.
	 * 
	 * @return Medical Review Static data
	 */
	@GetMapping("/medical-review")
	public SuccessResponse<MedicalReviewStaticDataDTO> getMedicalReviewStaticData() {
		return new SuccessResponse<>(SuccessCode.GET_MEDICAL_REVIEW_STATIC_DATA,
				staticDataService.getMedicalReviewStaticData(), HttpStatus.OK);
	}

}
