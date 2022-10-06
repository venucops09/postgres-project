package com.mdtlabs.coreplatform.spiceservice.glucoseLog.repository;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;


/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the GlucoseLog module action in database.
 * In query annotation (nativeQuery = true) the below query perform like SQL.
 * Otherwise its perform like HQL default value for nativeQuery FALSE
 * </p>
 * 
 * @author Karthick Murugesan
 */
@Repository
public interface GlucoseLogRepository extends JpaRepository<GlucoseLog, Long> {
    public static final String GET_GLUCOSE_LOG_BY_PATIENT_TRACK_ID =
            "select glucoselog from GlucoseLog as glucoselog where " +
            "glucoselog.patientTrackId = :patientTrackId and isLatest=true and " +
            "glucoselog.createdAt > :yesterday";

    public static final String UPDATE_GLUCOSE_LOG_STATUS = 
    "update GlucoseLog as glucoselog set glucoselog.isLatest=:isLatest where glucoselog.id=:id";

    public static final String GET_GLUCOSE_LOGS = 
    "Select * from glucoselog where patient_track_id=:patientTrackId and " +
	"is_deleted=false order by modified_at DESC";

    /**
     * This method fetches a single Glucose log by patient track id.
     *
     * @param patientTrackId
     * @return GlucoseLog Entity
     * @author Victor Jefferson
     */
    @Query(value = GET_GLUCOSE_LOG_BY_PATIENT_TRACK_ID)
    public GlucoseLog findByPatientTrackIdAndIsCreatedToday(@Param("patientTrackId") long patientTrackId, @Param("yesterday") Date date);

    /**
     * Finds the patient tracker by id
     * 
     * @param patientTrackerId
     * @param isDeleted
     * @param isLatest
     * @return
     */
    public GlucoseLog findByPatientTrackIdAndIsDeletedAndIsLatest(
                        Long patientTrackId,
                        boolean isDeleted, boolean isLatest);


    /**
     * Updates the GlucoseLog latest status
     * 
     * @param id
     * @param isLatest
     * @return
     */
    @Transactional
    @Modifying
    @Query(value = UPDATE_GLUCOSE_LOG_STATUS)
    public int updateGlucoseLogLatestStatus(@Param("id") long id, @Param("isLatest") boolean isLatest);

    @Query(value = GET_GLUCOSE_LOGS, nativeQuery = true)
    public Page<GlucoseLog> getGlucoseLogs(@Param("patientTrackId") Long id, Pageable pageable);
}
