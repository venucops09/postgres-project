package com.mdtlabs.coreplatform.spiceadminservice.medication.controller;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.model.dto.spice.MedicationDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.OtherMedicationDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.Medication;
import com.mdtlabs.coreplatform.spiceadminservice.medication.service.MedicationService;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessResponse;

import io.swagger.annotations.Api;

/**
 * This class is a controller class to perform operation on medication entities.
 *
 * @author Niraimathi
 */
@RestController
@RequestMapping(value = "/medication")
@Validated
@Api(basePath = "/medication", value = "master_data", description = "Medication related APIs", produces = "application/json")
public class MedicationController {
	private static final List<String> noDataList = Arrays.asList(Constants.NO_DATA_FOUND);

	@Autowired
	private MedicationService medicationService;

	ModelMapper modelMapper = new ModelMapper();

	/**
	 * This method is used to add a new medication.
	 *
	 * @param medications List of medication
	 * @return List of medication entities
	 */
	@RequestMapping(method = RequestMethod.POST)
	public SuccessResponse<List<MedicationDTO>> addMedication(@Valid @RequestBody List<Medication> medications) {
		medicationService.addMedication(medications);
		return new SuccessResponse<>(SuccessCode.MEDICATION_SAVE, HttpStatus.CREATED);
	}

	/**
	 * Used to update a medication detail like name, etc.,
	 *
	 * @param medication
	 * @return Medication Entity
	 */
	@RequestMapping(method = RequestMethod.PUT)
	public SuccessResponse<Medication> updateMedication(@Valid @RequestBody Medication medication) {
		medicationService.updateMedication(medication);
		return new SuccessResponse<Medication>(SuccessCode.MEDICATION_UPDATE, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve single medication's details using
	 * medicationId
	 *
	 * @param requestDTO
	 * @return Medication Entity
	 */
	@RequestMapping(value = "/details", method = RequestMethod.GET)
	public SuccessResponse<Medication> getMedicationById(@RequestBody RequestDTO requestDTO) {
		return new SuccessResponse<Medication>(SuccessCode.GET_MEDICATION,
				medicationService.getMedicationById(requestDTO), HttpStatus.OK);
	}

	/**
	 * This method is used to retreive all medication details.
	 *
	 * @param requestObject
	 * @return List of Medication Entity
	 */
	@RequestMapping(method = RequestMethod.GET)
	public SuccessResponse<List<MedicationDTO>> getAllMedications(@RequestBody RequestDTO requestObject) {
		List<Medication> medications = medicationService.getAllMedications(requestObject);
		if (!medications.isEmpty()) {
			List<MedicationDTO> medicationDTOs = modelMapper.map(medications, new TypeToken<List<MedicationDTO>>() {
			}.getType());
			return new SuccessResponse<List<MedicationDTO>>(SuccessCode.GET_MEDICATIONS, medicationDTOs,
					medicationDTOs.size(), HttpStatus.OK);
		}
		return new SuccessResponse<List<MedicationDTO>>(SuccessCode.GET_MEDICATIONS, noDataList, 0, HttpStatus.OK);
	}

	/**
	 * Used to soft delete a medication.
	 *
	 * @param requestDTO
	 * @return Boolean
	 */
	@RequestMapping(value = "/remove", method = RequestMethod.DELETE)
	public SuccessResponse<Boolean> deleteMedicationById(@RequestBody RequestDTO requestDTO) {
		medicationService.deleteMedicationById(requestDTO);
		return new SuccessResponse<Boolean>(SuccessCode.MEDICATION_STATUS_UPDATE, HttpStatus.OK);
	}

	/**
	 * Search and get medications list based on country.
	 *
	 * @param requestObject
	 * @return List of Medication entity
	 */
	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public SuccessResponse<List<MedicationDTO>> searchMedications(@RequestBody RequestDTO requestObject) {
		List<Medication> medications = medicationService.searchMedications(requestObject);
		if (!medications.isEmpty()) {
			List<MedicationDTO> medicationDTOs = modelMapper.map(medications, new TypeToken<List<MedicationDTO>>() {
			}.getType());
			return new SuccessResponse<List<MedicationDTO>>(SuccessCode.GET_MEDICATIONS, medicationDTOs,
					medicationDTOs.size(), HttpStatus.OK);
		}
		return new SuccessResponse<List<MedicationDTO>>(SuccessCode.GET_MEDICATIONS, noDataList, 0, HttpStatus.OK);
	}

	/**
	 * Used to validate a medication.
	 *
	 * @param medication
	 * @return boolean based on validation
	 */
	@RequestMapping(value = "/validate", method = RequestMethod.POST)
	public SuccessResponse<Boolean> validateMedication(@RequestBody Medication medication) {
		medicationService.validateMedication(medication);
//        return new ResponseEntity<Boolean>(medicationService.validateMedication(medication), HttpStatus.OK);
		return new SuccessResponse<>(SuccessCode.VALIDATE_MEDICATION, HttpStatus.OK);
	}

	/**
	 * To get the other medication details
	 * 
	 * @param countryId
	 * @return OtherMedicationDTO
	 */
	@GetMapping("/other-medication/{countryId}")
//	@TokenParse
	public ResponseEntity<OtherMedicationDTO> getOtherMedications(@PathVariable("countryId") long countryId) {

		Medication medication = medicationService.getOtherMedication(countryId);

		OtherMedicationDTO otherMedicationDTO = modelMapper.map(medication, new TypeToken<OtherMedicationDTO>() {
		}.getType());
//		System.out.println("other medication dto in controller" + otherMedicationDTO.toString());
//		return new SuccessResponse<OtherMedicationDTO>(SuccessCode.GET_OTHER_MEDICATION, otherMedicationDTO,
//				HttpStatus.OK);
		return new ResponseEntity<OtherMedicationDTO>(otherMedicationDTO, HttpStatus.OK);
	}

}
