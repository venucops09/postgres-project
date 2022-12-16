package com.mdtlabs.coreplatform.spiceservice.patient.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.model.dto.spice.EnrollmentRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.EnrollmentResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.GetRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.MyPatientListDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientGetRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientTrackerDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PregnancyRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchPatientListDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientPregnancyDetails;
import com.mdtlabs.coreplatform.common.util.EnrollmentInfo;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceservice.patient.service.PatientService;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;

/**
 * This is the conntroller class for the patient entity. It maintains the
 * request and response for the Patient Entity.
 *
 * @author Niraimathi S
 */
@RestController
@RequestMapping(value = "/patient")
//@Validated
public class PatientController {
	@Autowired
	private PatientService patientService;

	@Autowired
	private PatientTrackerService patientTrackerService;
	
	private ModelMapper modelMapper = new ModelMapper();

	/**
	 * This method is used to add a patient.
	 * 
	 * @param patient
	 * @return Patient Entity
	 * @author Niraimathi S
	 * @throws InterruptedException
	 */
	@RequestMapping(value = "/enrollment", method = RequestMethod.POST)
	public SuccessResponse<EnrollmentResponseDTO> createPatient(
			@Validated(EnrollmentInfo.class) @RequestBody EnrollmentRequestDTO patient) {
		return new SuccessResponse<>(SuccessCode.PATIENT_SAVE, patientService.createPatient(patient),
				HttpStatus.CREATED);
	}

	/**
	 * Gets Patient details.
	 *
	 * @param requestData Request data containing fields like patientTrackId,
	 *                    tenantId to get patient details.
	 * @return Patient Entity
	 * @author Niraimathi S
	 */
	@RequestMapping(value = "/details", method = RequestMethod.POST)
	public SuccessResponse<PatientTrackerDTO> getPatientDetails(@RequestBody PatientGetRequestDTO requestData) {
		return new SuccessResponse<PatientTrackerDTO>(SuccessCode.GET_PATIENT,
				patientService.getPatientDetails(requestData), HttpStatus.OK);
	}

	/**
	 * Create pregnancy details to the patient.
	 *
	 * @param requestData Request data containing pregnancy details
	 * @return PatientPregnancyDetails Entity.
	 * @author Niraimathi S
	 */
	@RequestMapping(value = "/pregnancy-details/create", method = RequestMethod.POST)
	public SuccessResponse<PregnancyRequestDTO> createPregnancyDetails(
			@RequestBody PregnancyRequestDTO requestData) {
		PatientPregnancyDetails pregnancyDetails = patientService.createPregnancyDetails(requestData);
		requestData.setId(pregnancyDetails.getId());
		return new SuccessResponse<PregnancyRequestDTO>(SuccessCode.PATIENT_PREGNANCY_SAVE,
				requestData, HttpStatus.CREATED);
	}

	/**
	 * Gets Pregnancy details of a patient.
	 *
	 * @param requestData Request data with patientTrackId and PatientPregnancy Id
	 * @return PatientPregnancyDetails entity.
	 * @author Niraimathi S
	 */
	@RequestMapping(value = "/pregnancy-details", method = RequestMethod.GET)
	public SuccessResponse<PregnancyRequestDTO> getPregnancyDetails(@RequestBody GetRequestDTO requestData) {
		PatientPregnancyDetails pregnancyDetails = patientService.getPregnancyDetails(requestData);
		PregnancyRequestDTO response = modelMapper.map(pregnancyDetails,
                new TypeToken<PregnancyRequestDTO>() {
                }.getType());
		return new SuccessResponse<PregnancyRequestDTO>(SuccessCode.GET_PATIENT_PREGNANCY,
				response, HttpStatus.OK);
	}

	/**
	 * Updates a patient's pregnancy details.
	 *
	 * @param requestData Request data containing updated pregnancy details.
	 * @return Updated PatientPregnancyDetails Entity.
	 * @author Niraimathi S
	 */
	@RequestMapping(value = "/pregnancy-details/update", method = RequestMethod.POST)
	public SuccessResponse<PatientPregnancyDetails> updatePregnancyDetails(
			@RequestBody PregnancyRequestDTO requestData) {
		return new SuccessResponse<PatientPregnancyDetails>(SuccessCode.UPDATE_PATIENT_PREGNANCY,
				patientService.updatePregnancyDetails(requestData), HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve patient details with filters like firstname,
	 * lastname, medical review date etc.,
	 *
	 * @param patientRequestDTO
	 * @return List of patient tracker entity
	 * @author Jeyaharini T A
	 */
	@PostMapping("/list")
	public SuccessResponse<List<MyPatientListDTO>> getMyPatientsList(@RequestBody PatientRequestDTO patientRequestDTO) {

		Map<String, Object> responseMap = patientTrackerService.listMyPatients(patientRequestDTO);

		List<MyPatientListDTO> patientListDTO = responseMap.containsKey("patientList")
				? (List<MyPatientListDTO>) (responseMap.get("patientList"))
				: new ArrayList<>();

		Integer totalCount = (responseMap.containsKey("totalCount") && !Objects.isNull(responseMap.get("totalCount")))
				? Integer.parseInt(responseMap.get("totalCount").toString())
				: null;

		if (Objects.isNull(totalCount)) {
			return new SuccessResponse<List<MyPatientListDTO>>(SuccessCode.SEARCH_PATIENTS, patientListDTO,
					HttpStatus.OK);
		}
		
		return new SuccessResponse<List<MyPatientListDTO>>(SuccessCode.GET_MY_PATIENTS_LIST, patientListDTO, totalCount,
				HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve patient details with filters like firstname,
	 * lastname, medical review date etc.,
	 *
	 * @param patientRequestDTO
	 * @return List of patient tracker entity
	 * @author Jeyaharini T A
	 */
	@PostMapping("/search")
	public SuccessResponse<List<SearchPatientListDTO>> searchPatients(
			@RequestBody PatientRequestDTO patientRequestDTO) {
		Map<String, Object> responseMap = patientTrackerService.searchPatients(patientRequestDTO);

		List<SearchPatientListDTO> patientListDTO = responseMap.containsKey("patientList")
				? (List<SearchPatientListDTO>) responseMap.get("patientList")
				: new ArrayList<>();

		Integer totalCount =(responseMap.containsKey("totalCount") && !Objects.isNull(responseMap.get("totalCount")))
				? Integer.parseInt(responseMap.get("totalCount").toString())
				: null;

		if (Objects.isNull(totalCount)) {
			return new SuccessResponse<List<SearchPatientListDTO>>(SuccessCode.SEARCH_PATIENTS, patientListDTO,
					HttpStatus.OK);
		}
		
		return new SuccessResponse<List<SearchPatientListDTO>>(SuccessCode.SEARCH_PATIENTS, patientListDTO, totalCount,
				HttpStatus.OK);
	}

	@PostMapping("/advance-search/country")
	public SuccessResponse<List<MyPatientListDTO>> getCountryWisePatientsWithAdvanceSearch(
			@RequestBody PatientRequestDTO patientRequestDTO) {
		if (!Objects.isNull(patientRequestDTO.getOperatingUnitId())) {
			patientRequestDTO.setIsGlobally(Constants.BOOLEAN_TRUE);
		}
		Map<String, Object> responseMap = patientTrackerService.patientAdvanceSearch(patientRequestDTO);
		List<SearchPatientListDTO> patientListDTO = (responseMap.containsKey("totalCount") && !Objects.isNull(responseMap.get("totalCount")))
				? (List<SearchPatientListDTO>) responseMap.get("patientList")
				: new ArrayList<>();

		Integer totalCount = (responseMap.containsKey("totalCount") && !Objects.isNull(responseMap.get("totalCount")))
				? Integer.parseInt(responseMap.get("totalCount").toString())
				: null;

		if (Objects.isNull(totalCount)) {
			return new SuccessResponse<List<MyPatientListDTO>>(SuccessCode.SEARCH_PATIENTS, patientListDTO,
					HttpStatus.OK);
		}
		
		return new SuccessResponse<List<MyPatientListDTO>>(SuccessCode.SEARCH_PATIENTS, patientListDTO, totalCount,
				HttpStatus.OK);
	}

	@PostMapping("/advance-search/site")
	public SuccessResponse<List<MyPatientListDTO>> getPatientsWithAdvanceSearch(
			@RequestBody PatientRequestDTO patientRequestDTO) {
		Map<String, Object> responseMap = patientTrackerService.patientAdvanceSearch(patientRequestDTO);
		List<SearchPatientListDTO> patientListDTO = (responseMap.containsKey("totalCount") && !Objects.isNull(responseMap.get("totalCount")))
				? (List<SearchPatientListDTO>) responseMap.get("patientList")
				: new ArrayList<>();

		Integer totalCount = (responseMap.containsKey("totalCount") && !Objects.isNull(responseMap.get("totalCount")))
				? Integer.parseInt(responseMap.get("totalCount").toString())
				: null;

		if (Objects.isNull(totalCount)) {
			return new SuccessResponse<List<MyPatientListDTO>>(SuccessCode.SEARCH_PATIENTS, patientListDTO,
					HttpStatus.OK);
		}
		
		return new SuccessResponse<List<MyPatientListDTO>>(SuccessCode.SEARCH_PATIENTS, patientListDTO, totalCount,
				HttpStatus.OK);
	}
}
