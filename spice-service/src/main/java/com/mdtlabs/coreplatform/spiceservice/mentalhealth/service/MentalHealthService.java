package com.mdtlabs.coreplatform.spiceservice.mentalhealth.service;

import com.mdtlabs.coreplatform.common.model.dto.spice.MentalHealthDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.MentalHealth;

/**
 * This is an interface to perform any actions in mentalHealth related entities
 * 
 * @author Karthick Murugesan
 *
 */
public interface MentalHealthService {

	/**
	 * Creates the Mental health details for patient
	 * 
	 * @param mentalHealth
	 * @return MentalHealth
	 * @author Karthick Murugesan
	 */
	public MentalHealth createOrUpdateMentalHealth(MentalHealth mentalHealth);

	/**
	 * Gets a mental health details for a patient based on type
	 * 
	 * @param requestData a MentalHealthRequestDTO object
	 * @return MentalHealthDTO
	 * @author Karthick Murugesan
	 */
	public MentalHealthDTO getMentalHealthDetails(RequestDTO requestData);

	public MentalHealth createMentalHealth(MentalHealth mentalHealth);

	public void setPHQ4Score(MentalHealth mentalHealth);

}
