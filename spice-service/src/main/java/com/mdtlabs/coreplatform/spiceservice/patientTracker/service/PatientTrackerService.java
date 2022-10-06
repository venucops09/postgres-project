package com.mdtlabs.coreplatform.spiceservice.patientTracker.service;

import java.util.Date;
import java.util.List;


import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.model.dto.spice.ConfirmDiagnosisDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.MyPatientListDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchPatientListDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;


/**
 * This is an interface to perform any actions in PatientTracker related
 * entities
 * 
 * @author Karthick Murugesan
 *
 */
public interface PatientTrackerService {

	/**
	 * This method adds a new patient tracker.
	 *
	 * @param patientTracker
	 * @return PatientTracker Entity
	 * @author Victor Jefferson
	 */
	public PatientTracker addOrUpdatePatientTracker(PatientTracker patientTracker);

	/**
	 * This method fetches a patient tracker by id.
	 *
	 * @param patientTrackerId
	 * @return PatientTracker Entity
	 * @author Victor Jefferson
	 */
	public PatientTracker getPatientTrackerById(long patientTrackerId);

	public void UpdatePatientTrackerForBpLog(long patientTrackerId, BpLog bpLog, Date nextBpAssessmentDate);

	public void UpdatePatientTrackerForGlucoseLog(long patientTrackerId, GlucoseLog glucoseLog, Date nextBgAssessmentDate);

	public PatientTracker getPatientTrackerByNationalId(String nationalId);

	// public void setPHQ4Score(PatientTracker patientTracker, MentalHealthDTO mentalHealth);

	public PatientTracker findByNationalIdIgnoreCase(String searchNationalId);

	/**
	 * This method is used to list the patient details
	 * 
	 * @param patientRequestDTO
	 * @return List<MyPatientListDTO>
	 * @author Jeyaharini T A
	 */
	public List<MyPatientListDTO> listMyPatients(PatientRequestDTO patientRequestDTO);

	/**
	 * This method is used to search the patient with fields like national id,
	 * program id etc.,
	 * 
	 * @param patientRequestDTO
	 * @return List<SearchPatientListDTO>
	 * @author Jeyaharini T A
	 */
	public List<SearchPatientListDTO> searchPatients(PatientRequestDTO patientRequestDTO);

	/**
	 * This method is used to get the patients list with advance search.
	 * 
	 * @param patientRequestDTO
	 * @return List<MyPatientListDTO>
	 * @author Jeyaharini T A
	 */
	public List<MyPatientListDTO> patientAdvanceSearch(PatientRequestDTO patientRequestDTO);

	/**
	 * Updates isLabTestReferred based on labtests referred to a patient.
	 *
	 * @param patientTrackId PatientTrackId
	 * @param tenantId tenantId
	 * @param isLabTestReferred isLabTestReferred field
	 * @author Niraimathi S
	 */
    void updatePatientTrackerLabtestReferral(long patientTrackId, Long tenantId, boolean isLabTestReferred);

	/*
	 * Update confirm diagnosis details to the patient.
	 * 
	 * @param confirmDiagnosis
	 * @return ConfirmDiagnosisDTO
	 */
	public ConfirmDiagnosisDTO updateConfirmDiagnosis(ConfirmDiagnosisDTO confirmDiagnosis);

	public void updateForFillPrescription(Long id, boolean isMedciationPrescribed);

}
