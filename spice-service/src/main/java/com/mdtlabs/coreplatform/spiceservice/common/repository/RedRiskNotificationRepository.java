package com.mdtlabs.coreplatform.spiceservice.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.RedRiskNotification;


/**
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the RedRiskNotification module action in database.
 * 
 * @author Karthick Murugesan
 */
@Repository
public interface RedRiskNotificationRepository extends JpaRepository<RedRiskNotification, Long> {


    public static final String UPDATE_REDRISK_STATUS = 
    "update RedRiskNotification as redrisk set redrisk.status = :updatedStatus where redrisk.isDeleted=false " +
    "and redrisk.patientTrackId=:patientTrackId and redrisk.status=:status";
    
    /**
     * Update the red risk status of a patient
     * 
     * @param status
     * @param patientTrackId
     * @return
     */
    @Query(value = UPDATE_REDRISK_STATUS)
    public int updateRedRiskStatus(@Param("updatedStatus") String updatedStatus, @Param("patientTrackId") long patientTrackId, @Param("status") String status);
    
}
