package com.mdtlabs.coreplatform.spiceservice.patient.service;

import com.mdtlabs.coreplatform.common.model.dto.spice.EnrollmentRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.EnrollmentResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.GetRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientGetRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientTrackerDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PregnancyRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientPregnancyDetails;

/**
 * This is an interface to perform any actions in patient related entities
 *
 * @author Rajkumar
 */
public interface PatientService {
	/**
	 * This method adds a new patient.
	 *
	 * @param patient
	 * @return Patient Entity
	 * @author Rajkumar
	 */
	public EnrollmentResponseDTO createPatient(EnrollmentRequestDTO patient);

	/**
	 * Gets Patient details.
	 *
	 * @param requestData Request data containing fields like patientTrackId,
	 *                    tenantId to get patient details.
	 * @return Patient Entity
	 * @author Rajkumar
	 */
	public PatientTrackerDTO getPatientDetails(PatientGetRequestDTO requestData);

	/**
	 * Create pregnancy details to the patient.
	 *
	 * @param requestData Request data containing pregnancy details
	 * @return PatientPregnancyDetails Entity.
	 * @author Rajkumar
	 */
	public PatientPregnancyDetails createPregnancyDetails(PregnancyRequestDTO requestData);

	/**
	 * Gets Pregnancy details of a patient.
	 *
	 * @param requestData Request data with patientTrackId and PatientPregnancy Id
	 * @return PatientPregnancyDetails entity.
	 * @author Rajkumar
	 */
	public PatientPregnancyDetails getPregnancyDetails(GetRequestDTO requestData);

	/**
	 * Updates a patients pregnancy details.
	 *
	 * @param requestData Request data containing updated pregnancy details.
	 * @return Updated Patient's pregnancy details.
	 * @author Rajkumar
	 */
	public PatientPregnancyDetails updatePregnancyDetails(PregnancyRequestDTO requestData);
}
