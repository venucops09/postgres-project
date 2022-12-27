package com.mdtlabs.coreplatform.spiceservice.bplog.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;

/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the BpLog module action in database.
 * In query annotation (nativeQuery = true) the below query perform like SQL.
 * Otherwise its perform like HQL default value for nativeQuery FALSE
 * </p>
 * 
 * @author Rajkumar
 */
@Repository
public interface BpLogRepository extends JpaRepository<BpLog, Long> {

	static final String GET_BP_LOG_BY_PATIENT_TRACK_ID = "select bplog from BpLog as bplog where "
			+ "bplog.patientTrackId = :patientTrackId and isLatest=true and " + "bplog.createdAt > :yesterday";

	public static final String UPDATE_BP_LOG_LATEST_STATUS = "update BpLog as bplog set bplog.isLatest=:isLatest where bplog.patientTrackId=:patientTrackId";

	static final String GET_BP_LOGS = "Select * from bplog where patient_track_id=:patientTrackId and "
			+ "is_deleted=false order by modified_at DESC";

	/**
	 * Find Bplog by patientTracker and isDeleted and isLatest
	 * 
	 * @param patientTrackId
	 * @param isDeleted
	 * @param isLatest
	 * @return BpLog entity
	 */
	public BpLog findBypatientTrackIdAndIsDeletedAndIsLatest(long patientTrackId, boolean isDeleted, boolean isLatest);

	/**
	 * Find Bplog by patientTracker and isCreated within 24 hours
	 *
	 * @param patientTrackId
	 * @param date
	 * @return BpLog entity
	 */
	@Query(value = GET_BP_LOG_BY_PATIENT_TRACK_ID)
	BpLog findByPatientTrackIdAndIsCreatedToday(@Param("patientTrackId") long patientTrackId,
			@Param("yesterday") Date date);

	/**
	 * Get list of Bplogs by patientTracker
	 *
	 * @param patientTrackId
	 * @return BpLog entity
	 */

	public List<BpLog> findBypatientTrackId(long patientTrackId);

	/**
	 * Updates the BpLog latest status
	 * 
	 * @param id
	 * @param isLatest
	 * @return int number of changed rows
	 */
	@Transactional
	@Modifying
	@Query(value = UPDATE_BP_LOG_LATEST_STATUS)
	public int updateBpLogLatestStatus(@Param("patientTrackId") long id, @Param("isLatest") boolean isLatest);

	/**
	 * Gets BpLogs based on patientTrackId.
	 *
	 * @param id       PatientTrackId
	 * @param pageable Pageable object
	 * @return Pagable BpLog entity.
	 */
	@Query(value = GET_BP_LOGS, nativeQuery = true)
	Page<BpLog> getBpLogs(@Param("patientTrackId") Long id, Pageable pageable);

	public BpLog findByIdAndIsDeletedAndPatientTrackId(Long id, Boolean isDeleted, Long patientTrackId);

	public BpLog findFirstByPatientTrackIdAndIsDeletedOrderByBpTakenOnDesc(long patientTrackId, Boolean booleanFalse);

}
