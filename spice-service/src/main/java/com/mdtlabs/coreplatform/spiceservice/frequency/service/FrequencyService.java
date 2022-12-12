package com.mdtlabs.coreplatform.spiceservice.frequency.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.spice.Frequency;

/**
 * This is an interface to perform any actions in frequency entities
 * 
 * @author Niraimathi
 *
 */
public interface FrequencyService {

	/**
	 * Adds a new frequency .
	 * 
	 * @param frequency
	 * @return Frequency Entity.
	 */
	public Frequency addFrequency(Frequency frequency);

	/**
	 * Retrieves single frequency using frequency Id.
	 * 
	 * @param id
	 * @return Frequency Entity.
	 */
	public Frequency getFrequencyById(long id);

	/**
	 * Gets a frquency based on risk level value.
	 * 
	 * @param riskLevel
	 * @return Frequency Entity
	 */
	// public Frequency getFrequencyByRiskLevel(String riskLevel);

	public List<Frequency> getFrequencyListByRiskLevel(String riskLevel);

	/**
	 * Retrieves a frequency based on its name and type.
	 * 
	 * @param name
	 * @param type
	 * @return Frequency Entity.
	 */
	public Frequency getFrequencyByFrequencyNameAndType(String name, String type);

	public List<Frequency> findByIsDeletedFalseAndIsActiveTrue();
}
