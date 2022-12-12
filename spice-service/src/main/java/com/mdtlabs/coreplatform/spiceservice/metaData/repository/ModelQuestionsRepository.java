package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.spice.ModelQuestions;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * This interface is responsible for performing database operations between
 * server and ModelQuestions entity.
 * 
 * @author Niraimathi S
 *
 */
public interface ModelQuestionsRepository extends GenericRepository<ModelQuestions> {
	/**
	 * Gets list of ModelQuestions entities based on countryId and isDeleted fields
	 * 
	 * @param countryId country Id
	 * @param isDeleted isDeleted value
	 * @return List of ModelQuestions entities
	 */
	public List<ModelQuestions> findByCountryIdAndIsDeleted(Long countryId, boolean isDeleted);

	/**
	 * Gets list of ModelQuestions entities based on isDefault and isDeleted Fields.
	 * 
	 * @param isDefault isDefault value
	 * @param isDeleted isDeleted value
	 * @return List of ModelQuestions entities
	 */
	public List<ModelQuestions> findByIsDefaultAndIsDeleted(boolean isDefault, boolean isDeleted);
}
