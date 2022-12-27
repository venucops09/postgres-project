package com.mdtlabs.coreplatform.spiceadminservice.accountcustomization.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.AccountCustomization;

/**
 * This repository interface has the needed customized functions for account
 * customization
 * 
 * @author Rajkumar
 *
 */
@Repository
public interface AccountCustomizationRepository extends JpaRepository<AccountCustomization, Long> {

	public static final String GET_ACCOUNT_CUSOMIZATION_WITH_CONDITIONS = "SELECT accountcustomization FROM AccountCustomization as accountcustomization "
			+ " WHERE (accountcustomization.countryId = :countryId) AND (accountcustomization.accountId = :accountId) AND (:category IS null OR accountcustomization.category = :category)"
			+ " AND (:type IS null OR upper(accountcustomization.type) = upper(:type))"
			+ " AND (:clinicalWorkflowId IS null OR workflow_id = :clinicalWorkflowId)  AND accountcustomization.isDeleted= :isDeleted";

	public static final String REMOVE_ACCOUNT_CUSTOMIZATION = "UPDATE AccountCustomization SET isDeleted = :isDeleted, tenantId = :tenantId WHERE id = :id ";

	/**
	 * Gets a Account customization by Id And Is Deleted.
	 * 
	 * @param id
	 * @param isDeleted
	 * @return AccountCustomization entity.
	 * @author Rajkumar
	 */
	public AccountCustomization findByIdAndIsDeleted(Long id, Boolean isDeleted);

	/**
	 * To get a Account customization details with conditions
	 * 
	 * @param countryId
	 * @Param accountId
	 * @param category
	 * @param type
	 * @return AccountCustomization entity
	 * @author Rajkumar
	 */
	@Query(value = GET_ACCOUNT_CUSOMIZATION_WITH_CONDITIONS)
	public AccountCustomization getAccountCustomization(@Param("countryId") Long countryId,
			@Param("accountId") Long accountId, @Param("category") String category, @Param("type") String type,
			@Param("clinicalWorkflowId") Long clinicalWorklowId, @Param("isDeleted") boolean isDeleted);

	/**
	 * To remove a account customization
	 * 
	 * @param isDeleted
	 * @param tenantId
	 * @return AccountCustomization
	 * @author Rajkumar
	 */
	@Modifying
	@Transactional
	@Query(value = REMOVE_ACCOUNT_CUSTOMIZATION)
	public void removeAccountCustomization(@Param("isDeleted") boolean isDeleted, @Param("tenantId") long tenantId,
			@Param("id") long id);
			
	/**
	 * Gets list by countryID, category list and type list.
	 * 
	 * @param countryId countryId
	 * @param category list of category
	 * @param screenTypes lsit of types
	 * @return List of AccountCustomization entites.
	 */
	public List<AccountCustomization> findByCountryIdAndCategoryInAndTypeIn(Long countryId, List<String> category,
			List<String> screenTypes);
}
