package com.mdtlabs.coreplatform.spiceservice.symptom.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.spice.Symptom;


public interface SymptomService {

	/**
	 * This method returns list of all symptoms
	 *
	 * @return List of Symptom Entity
	 * @author Victor Jefferson
	 */
	public List<Symptom> getAllSymptoms();

	/**
	 * This method adds a symptom
	 *
	 * @param symptom
	 * @return Symptom Entity
	 * @author Victor Jefferson
	 */
	public Symptom addSymptom(Symptom symptom);

}
