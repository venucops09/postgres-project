package com.mdtlabs.coreplatform.spiceadminservice.medication.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.Medication;


/**
 * This is an interface to perform any actions in medication entities
 * 
 * @author Niraimathi
 *
 */
public interface MedicationService {

	/**
	 * This method is used to add a new medication.
	 * 
	 * @param medication
	 * @return Medication Entity
	 */
	public List<Medication> addMedication(List<Medication> medication);

	/**
	 * This method is used to update a medication details like name.
	 * 
	 * @param medication
	 * @return Medication Entity
	 */
	public Medication updateMedication(Medication medication);

	/**
	 * This method retrieves a single medication's details.
	 * 
	 * @param medicationId
	 * @return Medication Entity
	 */
	public Medication getMedicationById(RequestDTO requestDTO);

	/**
	 * Retrieves all medication's details
	 * 
	 * @param requestObject
	 * @return List of Medication Entity
	 */
	public List<Medication> getAllMedications(RequestDTO requestObject);

	/**  
	 * This method is used to update the status of a medication which is soft
	 * deleted.
	 * 
	 * @param status
	 * @param medicationId
	 * @return Boolean
	 */
	public Boolean deleteMedicationById(RequestDTO requestDTO);

	/**
	 * This method used to get medication based on country.
	 * 
	 * @param searchTerm
	 * @param countryId
	 * @return List of medication entities.
	 */
	public List<Medication> searchMedications(RequestDTO requestObject);

	/**
	 * Used to validate medication details.
	 * 
	 * @param medication
	 * @return boolean
	 */
	public Boolean validateMedication(Medication medication);

	/**
	 * Used to get other medication details
	 * 
	 * @param countryId
	 * @return Medication entity
	 */
	public Medication getOtherMedication(long countryId);
}
