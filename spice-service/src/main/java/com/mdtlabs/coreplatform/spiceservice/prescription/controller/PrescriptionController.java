package com.mdtlabs.coreplatform.spiceservice.prescription.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdtlabs.coreplatform.common.Constants;
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
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceservice.prescription.service.PrescriptionService;

import io.swagger.annotations.Api;

/**
 * This class is a controller class to perform operation on prescription
 * entities.
 * 
 * @author Jeyaharini T A
 *
 */
@RestController
@Validated
@Api(basePath = "/prescription", value = "master_data", description = "Prescription related APIs", produces = "application/json")
public class PrescriptionController {

	@Autowired
	private PrescriptionService prescriptionService;

	private static final List<String> noDataList = Arrays.asList(Constants.NO_DATA_FOUND);

	/**
	 * To create or update the prescription details
	 * 
	 * @param prescriptionRequest
	 * @param signatureFile
	 * @return List of updated prescriptions
	 */
	@PostMapping("/prescription/update")
	public SuccessResponse<List<Prescription>> addPrescription(
			@RequestParam("prescriptionRequest") String prescriptionRequest,
			@RequestParam("signatureFile") MultipartFile signatureFile) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			PrescriptionRequestDTO prescriptionRequestDTO = mapper.readValue(prescriptionRequest,
					PrescriptionRequestDTO.class);
			prescriptionRequestDTO.setSignatureFile(signatureFile);

			prescriptionService.createOrUpdatePrescription(prescriptionRequestDTO);
//			if (!prescriptionList.isEmpty()) {
//				return new SuccessResponse<List<Prescription>>(SuccessCode.PRESCRIPTION_SAVE, prescriptionList,
//						prescriptionList.size(), HttpStatus.OK);
//			}
//			return new SuccessResponse<List<Prescription>>(SuccessCode.PRESCRIPTION_SAVE, noDataList, 0, HttpStatus.OK);

		} catch (JsonMappingException me) {
			me.printStackTrace();
		} catch (JsonProcessingException pe) {
			pe.printStackTrace();
		}
		return new SuccessResponse<>(SuccessCode.PRESCRIPTION_SAVE, HttpStatus.CREATED);
	}

	/**
	 * To list the prescriptions based on the conditions like prescription id,
	 * patient visit id and patient track id etc.,
	 * 
	 * @param prescriptionListRequestDTO
	 * @return list of prescriptions
	 */
	@GetMapping("/prescription/list")
	public SuccessResponse<List<PrescriptionDTO>> listPrescription(@RequestBody RequestDTO prescriptionListRequestDTO) {
		List<PrescriptionDTO> prescriptionList = prescriptionService.getPrescriptions(prescriptionListRequestDTO);
		if (!prescriptionList.isEmpty()) {
			return new SuccessResponse<List<PrescriptionDTO>>(SuccessCode.PRESCRIPTION_GET, prescriptionList,
					prescriptionList.size(), HttpStatus.OK);
		}

		return new SuccessResponse<List<PrescriptionDTO>>(SuccessCode.PRESCRIPTION_GET, noDataList, 0, HttpStatus.OK);
	}

	/**
	 * To list the prescription history details
	 * 
	 * @param prescriptionListRequestDTO
	 * @return list of prescription history
	 */
	@GetMapping(path = "/prescription-history/list")
	public SuccessResponse<List<PrescriptionHistoryResponse>> listPrescriptionHistoryData(
			@RequestBody RequestDTO prescriptionListRequestDTO) {
		PrescriptionHistoryResponse prescriptionList = prescriptionService
				.listPrescriptionHistoryData(prescriptionListRequestDTO);
//		if (!prescriptionList.isEmpty()) {
		return new SuccessResponse<List<PrescriptionHistoryResponse>>(SuccessCode.PRESCRIPTION_HISTORY_GET,
				prescriptionList, HttpStatus.OK);
//		}

//		return new SuccessResponse<List<PrescriptionHistoryResponse>>(SuccessCode.PRESCRIPTION_HISTORY_GET, noDataList,
//				0, HttpStatus.OK);
	}

	/**
	 * To remove the prescription by updating its is_deleted field
	 * 
	 * @param prescriptionListRequestDTO
	 * @return
	 */
	@PutMapping(path = "/prescription/remove")
	public SuccessResponse<String> removePrescription(@RequestBody RequestDTO prescriptionListRequestDTO) {
		prescriptionService.removePrescription(prescriptionListRequestDTO);
		return new SuccessResponse<String>(SuccessCode.PRESCRIPTION_DELETE, HttpStatus.OK);
	}

	/**
	 * To list the fill-prescription details
	 * 
	 * @param searchRequestDTO
	 * @return List of fill-prescription data's
	 */
	@GetMapping(path = "/fill-prescription/list")
	public SuccessResponse<List<FillPrescriptionResponseDTO>> listFillPrescription(
			@RequestBody SearchRequestDTO searchRequestDTO) {
		List<FillPrescriptionResponseDTO> prescriptionList = prescriptionService.getFillPrescriptions(searchRequestDTO);
		if (!prescriptionList.isEmpty()) {
			return new SuccessResponse<List<FillPrescriptionResponseDTO>>(SuccessCode.FILL_PRESCRIPTION_GET,
					prescriptionList, prescriptionList.size(), HttpStatus.OK);
		}

		return new SuccessResponse<List<FillPrescriptionResponseDTO>>(SuccessCode.FILL_PRESCRIPTION_GET, noDataList, 0,
				HttpStatus.OK);
	}

	/**
	 * To update the fill-prescription data along with its associated tables like
	 * fill-prescription history, prescription and prescription history
	 * 
	 * @param fillPrescriptionRequestDTO
	 * @return List of updated fill-prescriptions
	 */
	@PostMapping(path = "/fill-prescription/update")
	public SuccessResponse<List<FillPrescription>> updateFillPrescription(
			@RequestBody FillPrescriptionRequestDTO fillPrescriptionRequestDTO) {
		prescriptionService.updateFillPrescription(fillPrescriptionRequestDTO);
//		if (!prescriptionList.isEmpty()) {
//			return new SuccessResponse<List<FillPrescription>>(SuccessCode.FILL_PRESCRIPTION_UPDATE, prescriptionList,
//					prescriptionList.size(), HttpStatus.OK);
//		}

		return new SuccessResponse<List<FillPrescription>>(SuccessCode.FILL_PRESCRIPTION_UPDATE, HttpStatus.OK);
	}

	/**
	 * To list the last filled prescription history for the patient
	 * 
	 * @param searchRequestDTO
	 * @return List of latest fill prescription history
	 */
	@GetMapping(path = "/prescription/refill-history")
	public SuccessResponse<List<FillPrescriptionHistory>> getReFillPrescriptionHistory(
			@RequestBody SearchRequestDTO searchRequestDTO) {
		List<FillPrescriptionHistory> prescriptionList = prescriptionService
				.getRefillPrescriptionHistory(searchRequestDTO);
		if (!prescriptionList.isEmpty()) {
			return new SuccessResponse<List<FillPrescriptionHistory>>(SuccessCode.REFILL_PRESCRIPTION_GET,
					prescriptionList, prescriptionList.size(), HttpStatus.OK);
		}

		return new SuccessResponse<List<FillPrescriptionHistory>>(SuccessCode.REFILL_PRESCRIPTION_GET, noDataList, 0,
				HttpStatus.OK);
	}

}
