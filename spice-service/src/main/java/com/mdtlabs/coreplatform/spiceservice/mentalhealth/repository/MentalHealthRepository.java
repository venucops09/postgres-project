package com.mdtlabs.coreplatform.spiceservice.mentalhealth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mdtlabs.coreplatform.common.model.entity.spice.MentalHealth;


/**
 * This repository class is responsible for communication between database and server side.
 *
 * @author Rajkumar
 */
@Repository
public interface MentalHealthRepository extends JpaRepository<MentalHealth, Long> {

    public static final String UPDATE_LATEST_STATUS =
    "Update MentalHealth as mentalhealth set mentalhealth.isLatest=false where mentalhealth.patientTrackId=:patientTrackId and mentalhealth.isLatest=true and mentalhealth.isDeleted=false"; 

    public MentalHealth findByPatientTrackIdAndIsDeletedAndIsLatest(Long patientTrackId, Boolean isDeleted, Boolean isLatest);

    public MentalHealth findByIdAndIsDeleted(Long id, Boolean isDeleted);

    @Modifying
    @Transactional
    @Query(value = UPDATE_LATEST_STATUS)
    public int updateLatestStatus(@Param("patientTrackId")long patientTrackId);
    
}
