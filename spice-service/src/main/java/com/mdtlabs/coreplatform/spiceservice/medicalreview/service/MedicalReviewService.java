package com.mdtlabs.coreplatform.spiceservice.medicalreview.service;

import java.util.Map;

import com.mdtlabs.coreplatform.common.model.dto.spice.MedicalReviewDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.MedicalReviewResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;

/**
 * This is an interface to perform any actions in medical review related
 * entities
 * 
 * @author Karthick Murugesan
 *
 */
public interface MedicalReviewService {

	/**
	 * Create patient medical review data(Initail medical review and continuous
	 * medical review)Creates a new medical review
	 * 
	 * @param medicalReviewDTO
	 * @return
	 */
	public MedicalReviewDTO addMedicalReview(MedicalReviewDTO medicalReviewDTO);

	public MedicalReviewResponseDTO getMedicalReviewSummaryHistory(RequestDTO medicalReviewLis);

	public MedicalReviewResponseDTO getMedicalReviewHistory(RequestDTO medicalReviewLis);

	public Map<String, Integer> getPrescriptionAndLabtestCount(RequestDTO request);

}
