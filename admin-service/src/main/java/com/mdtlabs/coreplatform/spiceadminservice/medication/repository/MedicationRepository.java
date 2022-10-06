package com.mdtlabs.coreplatform.spiceadminservice.medication.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Medication;

/**
 * 
 * This class is a repository class to establish communication between database
 * and server side.
 * 
 * @author Niraimathi S
 *
 */
@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {


	public static final String GET_ALL_MEDICATIONS = "select * from medicationcountrydetail as medication where "
			+ "(:countryId is null or medication.country_id=:countryId) and medication.tenant_id=:tenantId and medication.is_deleted=false "
			+ "and (:searchTerm is null or lower(medication.medication_name) LIKE CONCAT('%',lower(:searchTerm),'%'))"
			;

	public static final String UPDATE_MEDICATION_STATUS_BY_ID = "update from Medication as medication set "
			+ "medication.isDeleted=:status where medication.id = :medicationId AND medication.isDeleted=false AND "
			+ "medication.tenantId =:tenantId";

	public static final String GET_MEDICATION_BY_MEDICATION_NAME = "select medication from Medication as medication "
			+ "where lower(medication.medicationName) LIKE CONCAT('%',lower(:searchTerm),'%') AND medication.countryId=:countryId"
			+ " AND medication.isDeleted=false ORDER BY medication.medicationName ASC,medication.updatedAt DESC";

	public static final String GET_MEDICATION_BY_MANDATORY_FIELDS = "select medication from Medication as medication"
			+ " where (:countryId is null or medication.countryId=:countryId) AND (:medicationName is null or "
			+ "medication.medicationName=:medicationName) AND (:classificationId is null or medication.classificationId=:classificationId)"
			+ " AND (:brandId is null or medication.brandId=:brandId) AND (:dosageFormId is null "
			+ " or medication.dosageFormId=:dosageFormId)";
	public static final String GET_OTHER_MEDICATION = "select * from Medication as medication where medication.country_id = :countryId AND lower(medication.brand_name) LIKE CONCAT(lower(:brandName),'%') AND lower(medication.name) LIKE CONCAT(lower(:medicationName),'%') AND lower(medication.classification_name) LIKE CONCAT(lower(:classificationName),'%') AND lower(medication.dosage_form_name) LIKE CONCAT(:dosageFormName,'%') limit 1";

	/**
	 * This method is used to get all medications details.
	 *
	 * @param countryId
	 * @param pageable
	 * @return List of Medication Entities
	 */
	@Query(value = GET_ALL_MEDICATIONS, nativeQuery = true)
	public Page<Medication> getAllMedications(@Param("searchTerm") String searchTerm, @Param("countryId") Long countryId, @Param("tenantId") Long tenantId,
			Pageable pageable);

	/**
	 * This method is used to update the isdeleted status of a medication.
	 *
	 * @param status
	 * @param medicationId
	 * @return no.of updated rows
	 */
	@Modifying
	@Transactional
	@Query(value = UPDATE_MEDICATION_STATUS_BY_ID)
	public int updateMedicationById(@Param("status") Boolean status,
			@Param("medicationId") Long medicationId, @Param("tenantId") Long tenantId);

	/**
	 * This method retrives a single Medicaiton details using mandatory fields.
	 *
	 * @param classification
	 * @param brand
	 * @param dosageForm
	 * @param country
	 * @param name
	 * @return Medication Entity
	 */
	@Query(value = GET_MEDICATION_BY_MANDATORY_FIELDS)
	public Medication getMedicationByFields(@Param("classificationId") long classification,
			@Param("brandId") long brand, @Param("dosageFormId") long dosageForm, @Param("countryId") long country,
			@Param("medicationName") String name);

	/**
	 * Searches a medication based on the given condition
	 * 
	 * @param searchTerm
	 * @param countryId
	 * @return List of medication entities
	 */
	@Query(value = GET_MEDICATION_BY_MEDICATION_NAME)
	public List<Medication> searchMedications(@Param("searchTerm") String searchTerm,
			@Param("countryId") Long countryId);

	/**
	 * Searches a medication based on a given condition and return limited number of
	 * data.
	 * 
	 * @param searchTerm
	 * @param countryId
	 * @param pageable
	 * @return Limited number of Medication Entities.
	 */
	@Query(value = GET_ALL_MEDICATIONS, nativeQuery = true)
	public Page<Medication> searchMedicationsWithLimit(@Param("searchTerm") String searchTerm,
			@Param("countryId") Long countryId, @Param("tenantId") Long tenantId, Pageable pageable);

	/**
	 * This method retrives a single Medicaiton details using medicationId.
	 *
	 * @param id
	 * @param isDeleted
	 * @return Medication entity.
	 */
	public Medication getMedicationByIdAndIsDeleted(Long id, boolean isDeleted);

	/**
	 * To get other medication details
	 * 
	 * @param countryId
	 * @param medicationName
	 * @param brandName
	 * @param classificationName
	 * @param dosageFormName
	 * @return
	 */
	@Query(value = GET_OTHER_MEDICATION, nativeQuery = true)
	public Medication getOtherMedication(@Param("countryId") long countryId,
			@Param("medicationName") String medicationName, @Param("brandName") String brandName,
			@Param("classificationName") String classificationName, @Param("dosageFormName") String dosageFormName);

	/**
	 * Gets a medication based on its id  and tenantId
	 *
	 * @param id medication id
	 * @param tenantId tenant id
	 * @return Medication Entity
	 * @author Niraimathi S
	 */
	Medication findByIdAndIsDeletedFalseAndTenantId(Long id, Long tenantId);
}
