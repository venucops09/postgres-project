package com.mdtlabs.coreplatform.spiceservice.prescription.service;

import java.util.List;


import javax.validation.*;

import com.mdtlabs.coreplatform.common.model.dto.spice.FillPrescriptionRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.FillPrescriptionResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PrescriptionDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PrescriptionHistoryResponse;
import com.mdtlabs.coreplatform.common.model.dto.spice.PrescriptionRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.FillPrescription;
import com.mdtlabs.coreplatform.common.model.entity.spice.FillPrescriptionHistory;
import com.mdtlabs.coreplatform.common.model.entity.spice.Prescription;

public interface PrescriptionService {
	/**
	 * To create or update a prescription
	 * 
	 * @param prescriptionRequestDTO
	 * @return List of prescription
	 */
	public List<Prescription> createOrUpdatePrescription(@Valid PrescriptionRequestDTO prescriptionRequestDTO);

	/**
	 * To get the prescriptions
	 * 
	 * @param patientTrackId
	 * @param isDeleted
	 * @return PrescriptionDTO list
	 */
	public List<PrescriptionDTO> getPrescriptions(RequestDTO prescriptionListRequestDTO);

	/**
	 * To list the prescription history data's
	 * 
	 * @param prescriptionListRequestDTO
	 * @return
	 */
	public PrescriptionHistoryResponse listPrescriptionHistoryData(RequestDTO prescriptionListRequestDTO);

	/**
	 * To remove the prescription
	 * 
	 * @param prescriptionListRequestDTO
	 * @return boolean value true
	 */
	public boolean removePrescription(RequestDTO prescriptionListRequestDTO);

	/**
	 * To remove the fill prescription
	 * 
	 * @param prescriptionListRequestDTO
	 */
	public void removeFillPrescription(RequestDTO prescriptionListRequestDTO);

	/**
	 * To get fill prescriptions
	 * 
	 * @param searchRequestDTO
	 * @return list of fill prescriptions
	 */
	public List<FillPrescriptionResponseDTO> getFillPrescriptions(SearchRequestDTO searchRequestDTO);

	/**
	 * To update fill prescription
	 * 
	 * @param fillPrescriptionRequestDTO
	 * @return list of fill prescription
	 */
	public List<FillPrescription> updateFillPrescription(FillPrescriptionRequestDTO fillPrescriptionRequestDTO);

	/**
	 * To get the fill prescription histories
	 * 
	 * @param searchRequestDTO
	 * @return list of fill prescription history
	 */
	public List<FillPrescriptionHistory> getRefillPrescriptionHistory(SearchRequestDTO searchRequestDTO);

}
