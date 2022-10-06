package com.mdtlabs.coreplatform.spiceadminservice.labTestResultRanges.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.LabTestResultRange;

/**
 * This repository class maintains the custom functions needs for
 * LabTestResultRange module
 * 
 * @author Jeyaharini T A
 *
 */
@Repository
public interface LabTestResultRangesRepository extends JpaRepository<LabTestResultRange, Long> {

	public static final String REMOVE_LAB_TEST_RESULT_RANGE = "UPDATE LabTestResultRange SET isDeleted = :isDeleted WHERE id = :id";

	public static final String GET_LAB_TEST_RESULT_RANGES = "SELECT * FROM LabTestResultRange WHERE Id IN (:ids) AND is_deleted = :isDeleted";

	public LabTestResultRange findByIdAndIsDeleted(long id, boolean isDeleted);

	/**
	 * To update the isDeleted column status for the given id
	 * 
	 * @param id
	 * @param isDeleted
	 * @return int -> count of updated rows
	 */
	@Modifying
	@Transactional
	@Query(value = REMOVE_LAB_TEST_RESULT_RANGE)
	public int removeLabTestResultRange(@Param("id") long id, @Param("isDeleted") boolean isDeleted);

	/**
	 * To get the list of lab test result ranges based on the id's
	 * 
	 * @param ids
	 * @param isDeleted
	 * @return List of LabTestResultRange
	 */
	@Query(value = GET_LAB_TEST_RESULT_RANGES, nativeQuery = true)
	public List<LabTestResultRange> findByIdsAndIsDeleted(@Param("ids") List<Long> ids,
			@Param("isDeleted") boolean isDeleted);

	/**
	 * To get the list of lab test result range based on the lab test result id.
	 * 
	 * @param labTestResultId
	 * @param isDeleted
	 * @return list of lab test result range
	 */
	public List<LabTestResultRange> findByLabTestResultIdAndIsDeleted(long labTestResultId, boolean isDeleted);

}
