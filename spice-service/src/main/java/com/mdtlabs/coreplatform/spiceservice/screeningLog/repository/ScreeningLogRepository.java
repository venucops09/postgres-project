package com.mdtlabs.coreplatform.spiceservice.screeningLog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mdtlabs.coreplatform.common.model.entity.spice.ScreeningLog;


@Repository
public interface ScreeningLogRepository extends JpaRepository<ScreeningLog, Long> {
    public static final String UPDATE_LATEST_STATUS =
    "update ScreeningLog as screeninglog set screeninglog.isLatest=:isLatest where screeninglog.id=:id";

    public ScreeningLog findByIdAndIsDeletedAndIsLatest(long id, boolean isDeleted, boolean isLatest);


    @Transactional
    @Modifying
    @Query(value = UPDATE_LATEST_STATUS)
    public int updateLatestStatus(@Param("id") long id ,@Param("isLatest") boolean isLatest);

    ScreeningLog findByIdAndIsDeletedFalseAndIsLatestTrue(long id);
}
