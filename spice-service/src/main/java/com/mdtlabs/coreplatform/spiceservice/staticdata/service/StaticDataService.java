package com.mdtlabs.coreplatform.spiceservice.staticdata.service;

import com.mdtlabs.coreplatform.common.model.dto.spice.MedicalReviewStaticDataDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.StaticDataDTO;

public interface StaticDataService {

	/**
	 * TO get static data.
	 * 
	 * @return StaticDataDTO
	 */
	StaticDataDTO getStaticData();

	/**
	 * To get medical review related static data.
	 * 
	 * @return MedicalReviewStaticDataDTO
	 */
	MedicalReviewStaticDataDTO getMedicalReviewStaticData();

}
