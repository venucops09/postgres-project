package com.mdtlabs.coreplatform.spiceservice.assessment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.AssessmentLog;


@Repository
public interface AssessmentLogRepository extends JpaRepository<AssessmentLog, Long> {
    
}
