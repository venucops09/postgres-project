package com.mdtlabs.coreplatform.spiceservice.bplog.service;

import com.mdtlabs.coreplatform.common.model.dto.spice.PatientBpLogsDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;

/**
 * This is an interface to perform any actions in bplog related entities
 * 
 * @author Karthick Murugesan
 *
 */
public interface BpLogService {

	/**
	 * Creates a new bplog
	 * 
	 * @param bpLog
	 * @return BpLog entity
	 */
	public BpLog addBpLog(BpLog bpLog, boolean isPatientTrackerUpdate);

	/**
	 * Updates the bplog is test status
	 * 
	 * @param bpLog
	 */
	public void updateBpLogLatestStatus(BpLog bpLog);

	/**
	 * Fetches a single bp log by patient track id.
	 *
	 * @param patientTrackId
	 * @return BpLog Entity
	 */
	public BpLog getBpLogByPatientTrackId(long patientTrackId);

	/**
	 * Gets bplog based on patientTrackId and isLatest fields.
	 *
	 * @param patientTrackId Patient Track Id
	 * @param isLatest       isLatest field
	 * @return BPLog entity
	 */
	public BpLog getBpLogByPatientTrackIdAndIsLatest(long patientTrackId, boolean isLatest);

	/**
	 * Gets BPLog entity with symptoms.
	 *
	 * @param requestData Request data
	 * @return PatientBpLogsDTO
	 */
	public PatientBpLogsDTO getPatientBPLogsWithSymptoms(RequestDTO requestData);

	/**
	 * To convert the units of BpLog like height, weight and bmi
	 * 
	 * @param bpLog
	 * @param unit
	 * @return BpLog with converted units
	 */
	public BpLog convertBpLogUnits(BpLog bpLog, String unit);

}
