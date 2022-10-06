package com.mdtlabs.coreplatform.spiceservice.glucoseLog.service;

import com.mdtlabs.coreplatform.common.model.dto.spice.PatientGlucoseLogDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;

/**
 * This is an interface to perform any actions in bplog related entities
 * 
 * @author Karthick Murugesan
 *
 */
public interface GlucoseLogService {
    
    /**
     * This method adds a new Glucose log.
     *
     * @param glucoseLog
     * @return GlucoseLog Entity
     * @author Victor Jefferson
     */
    public GlucoseLog addGlucoseLog(GlucoseLog glucoseLog, boolean isPatientTrackerUpdate);

    /**
     * This method fetches a single glucose log.
     *
     * @param glucoseLogId
     * @return GlucoseLog Entity
     */
    public GlucoseLog getGlucoseLogById(long glucoseLogId);

    /**
     * This method fetches a single glucose log by patient track id.
     *
     * @param patientTrackId
     * @return GlucoseLog Entity
     */
    public GlucoseLog getGlucoseLogByPatientTrackId(long patientTrackId);

    public GlucoseLog getGlucoseLogByPatientTrackIdAndIsLatest(long patientTrackId, boolean isLatest);

    public PatientGlucoseLogDTO getPatientGlucoseLogsWithSymptoms(RequestDTO requestData);

}
