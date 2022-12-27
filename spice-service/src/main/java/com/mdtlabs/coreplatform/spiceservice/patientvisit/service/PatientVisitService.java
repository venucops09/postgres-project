package com.mdtlabs.coreplatform.spiceservice.patientvisit.service;

import java.util.List;
import java.util.Map;

import com.mdtlabs.coreplatform.common.model.dto.spice.CommonRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientVisit;


public interface PatientVisitService {

	public Map<String, Long> addPatientVisit(CommonRequestDTO patientVisitDTO);

	public PatientVisit getPatientVisitById(Long id);

	/**
	 * Retrieves the patientVisit to calculate patient visit dates based on the
	 * fields like patientTrackId, isInvestigation, isMedicalReview and
	 * isPrescription.
	 *
	 * @param patientTrackId
	 * @param isInvestigation
	 * @param isMedicalReview
	 * @param isPrescription
	 * @return List of PatientVisit Entities
	 * @author Rajkumar
	 */
	public List<PatientVisit> getPatientVisitDates(Long patientTrackId, Boolean isInvestigation,
			Boolean isMedicalReview, Boolean isPrescription);
	
	public PatientVisit getPatientVisit(Long id, Long tenantId);

	/**
	 * This method updates the PatientVisit details such as isInvestigation and
	 * tenantId based on visitId.
	 *
	 * @param isInvestigation
	 * @param visitId
	 * @return Number of rows affected.
	 * @author Rajkumar
	 */
	public PatientVisit updatePatientVisit(PatientVisit patientVisit);



}
