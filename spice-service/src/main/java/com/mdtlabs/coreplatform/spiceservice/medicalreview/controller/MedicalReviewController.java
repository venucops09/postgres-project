package com.mdtlabs.coreplatform.spiceservice.medicalreview.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.MedicalReviewDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.MedicalReviewResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.spiceservice.medicalreview.service.MedicalReviewService;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;

import io.swagger.annotations.Api;

/**
 * This class is a controller class to perform operation on MedicalReview
 * entity.
 *
 * @author Karthick Murugesan
 */
@RestController
@RequestMapping(value = "/medical-review")
@Validated
@Api(basePath = "/medical-review", value = "master_data", description = "MedicalReview related APIs", produces = "application/json")
public class MedicalReviewController {

	@Autowired
	private MedicalReviewService medicalReviewService;

	/**
	 * This method is used to add a medical review.
	 *
	 * @param medicalReviewDTO
	 * @return medicalReviewDTO Entity.
	 * @author Karthick Murugesan
	 */
	@RequestMapping(method = RequestMethod.POST)
	public SuccessResponse<MedicalReviewDTO> addMedicalReview(@RequestBody MedicalReviewDTO medicalReviewDTO) {
		medicalReviewService.addMedicalReview(medicalReviewDTO);
		return new SuccessResponse<>(SuccessCode.MEDICAL_REVIEW_SAVE, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/summary", method = RequestMethod.GET)
	public SuccessResponse<MedicalReviewResponseDTO> getMedicalReviewSummary(@RequestBody RequestDTO request) {
		return new SuccessResponse<>(SuccessCode.GET_MEDICAL_REVIEW_SUMMARY,
				medicalReviewService.getMedicalReviewSummaryHistory(request), HttpStatus.OK);
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public SuccessResponse<MedicalReviewResponseDTO> getMedicalReviewList(@RequestBody RequestDTO medicalReviewList) {
		return new SuccessResponse<>(SuccessCode.GET_MEDICAL_REVIEW_LIST,
				medicalReviewService.getMedicalReviewHistory(medicalReviewList), HttpStatus.OK);
	}

	@RequestMapping(value = "/count", method = RequestMethod.GET)
	public SuccessResponse<Map<String, Integer>> getMedicalReviewCount(@RequestBody RequestDTO request) {
		return new SuccessResponse<>(SuccessCode.GET_MEDICAL_REVIEW_COUNT,
				medicalReviewService.getPrescriptionAndLabtestCount(request), HttpStatus.OK);
	}
}
