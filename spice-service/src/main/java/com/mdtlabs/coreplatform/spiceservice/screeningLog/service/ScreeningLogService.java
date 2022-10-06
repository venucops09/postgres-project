package com.mdtlabs.coreplatform.spiceservice.screeningLog.service;

import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.ScreeningLogDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.common.model.entity.spice.ScreeningLog;

public interface ScreeningLogService {
    /**
     * This method adds a new screening log.
     *
     * @param screeningLog
     * @return ScreeningLog Entity
     * @author Victor Jefferson
     */
    public ScreeningLog addScreeningLog(ScreeningLogDTO screeningLog);


    /**
     * Creates a BpLog object based on the screening log
     * 
     * @param screeningLog
     * @param patientTracker
     * @return BpLog entity
     */
    public BpLog createBpLog(ScreeningLogDTO screeningLog, PatientTracker patientTracker);

    /**
     * Creates a GlucoseLog object based on the screening log
     * 
     * @param screeningLog
     * @param patientTracker
     * @return GlucoseLog entity
     */
    public GlucoseLog createGlucoseLog (ScreeningLogDTO screeningLog, 
                    PatientTracker patientTracker);

    public PatientTracker constructPatientTracker(PatientTracker patientTracker, ScreeningLogDTO screeningLogDTO);

    /**
     * Validates the ScreeningLog request data
     * 
     * @param screeningLog
     */
    public void validateScreeninigLog(ScreeningLogDTO screeningLog);

    public PatientTracker createPatientTracker(ScreeningLogDTO screeningLog);

//    public ScreeningLog getByIdAndIsLatestTrue(long patientTrackId, boolean isLatest);
    public ScreeningLog getByIdAndIsLatest(long patientTrackId);
    
    public ScreeningLog getScreeningDetails(RequestDTO screeningRequestDto);

    

}