package com.mdtlabs.coreplatform.spiceservice.patientlabtest.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.common.reflect.TypeToken;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.dto.spice.GetRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestResultDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestResultRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestResultResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.BaseEntity;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTest;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTestResult;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientLabTest;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientLabTestResult;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientVisit;
import com.mdtlabs.coreplatform.spiceservice.ApiInterface;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;
import com.mdtlabs.coreplatform.spiceservice.patientlabtest.repository.PatientLabTestRepository;
import com.mdtlabs.coreplatform.spiceservice.patientlabtest.repository.PatientLabTestResultRepository;
import com.mdtlabs.coreplatform.spiceservice.patientlabtest.service.PatientLabTestService;
import com.mdtlabs.coreplatform.spiceservice.patientvisit.service.PatientVisitService;

/**
 * This class implements the PatientLabTestService class and contains business
 * logic for the operations of PatientLabTest Entity.
 *
 * @author Niraimathi S
 */
@Service
public class PatientLabTestServiceImpl implements PatientLabTestService {

	@Autowired
	private PatientLabTestRepository patientLabTestRepository;

	@Autowired
	private PatientVisitService patientVisitService;

	@Autowired
	private PatientLabTestResultRepository patientLabTestResultRepository;

	@Autowired
	private PatientTrackerService patientTrackerService;

	@Autowired
	private ApiInterface apiInterface;

	/**
	 * {@inheritDoc}
	 */
	public List<PatientLabTest> createPatientLabTest(PatientLabTestRequestDTO requestData) {
		validateRequestData(requestData);
		boolean isOtherLabTest;
		isOtherLabTest = requestData.getLabTest().stream()
				.anyMatch(data -> data.getLabTestId().equalsIgnoreCase(Constants.OTHER));

		if (isOtherLabTest) {
			LabTest labTest = getOtherLabTest(requestData);
			requestData.getLabTest().forEach(entry -> {
				if (entry.getLabTestId().equalsIgnoreCase(Constants.OTHER)) {
					entry.setLabTestId(labTest.getId().toString());
				}
			});
		}
		Set<String> uniqueLabTestNames = new HashSet<>();
		Set<Long> uniqueLabTestIds = new HashSet<>();
		if (!Objects.isNull(requestData.getLabTest())) {
			for (PatientLabTestDTO test : requestData.getLabTest()) {
				test.setTenantId(requestData.getTenantId());
				test.setPatientTrackId(requestData.getPatientTrackId());
				test.setPatientVisitId(requestData.getPatientVisitId());
				test.setIsReviewed(test.getIsReviewed());
				uniqueLabTestNames.add(test.getLabTestName());
				uniqueLabTestIds.add(Long.parseLong(test.getLabTestId()));
			}
		}
		if (uniqueLabTestNames.size() != requestData.getLabTest().size()) {
			throw new BadRequestException(10006);
		}
		List<LabTest> labtests = getLabTestsByIds(uniqueLabTestIds);

		if (uniqueLabTestIds.size() != labtests.size()) {
			throw new DataNotFoundException(10007);
		}

		List<PatientLabTest> patientLabTests = constructPatientLabTestData(requestData);
		patientLabTests = patientLabTestRepository.saveAll(patientLabTests);
		PatientVisit patientVisit = patientVisitService.getPatientVisit(requestData.getPatientVisitId(),
				requestData.getTenantId());
		patientVisit.setInvestigation(true);
		patientVisitService.updatePatientVisit(patientVisit);
		updatePatientTrackerLabtestReferral(requestData.getPatientTrackId(), requestData.getTenantId(),
				Constants.BOOLEAN_TRUE);
		return patientLabTests;
	}

	/**
	 * {@inheritDoc}
	 */
	public PatientLabTestResponseDTO getPatientLabTestList(GetRequestDTO requestData) {
		if (Objects.isNull(requestData.getPatientTrackId())) {
			throw new DataNotAcceptableException(10010);
		}
		Long patientVisitId = requestData.getPatientVisitId();
		PatientLabTestResponseDTO response = new PatientLabTestResponseDTO();
		List<PatientVisit> patientVisitList = new ArrayList<>();
		List<PatientLabTest> labTests;

		if (requestData.isLatestRequired() && Objects.isNull(requestData.getPatientVisitId())) {
			patientVisitList = patientVisitService.getPatientVisitDates(requestData.getPatientTrackId(),
					Constants.BOOLEAN_TRUE, null, null);

			patientVisitId = 0 != patientVisitList.size() ? patientVisitList.get(patientVisitList.size() - 1).getId()
					: null;
			response.setPatientLabtestDates(
					patientVisitList.stream().map(visit -> Map.of("id", visit.getId(), "date", visit.getVisitDate()))
							.collect(Collectors.toList()));
			System.out.println(response.getPatientLabtestDates());
		}
		if (!Objects.isNull(requestData.getRoleName())
				&& requestData.getRoleName().equals(Constants.ROLE_LAB_TECHNICIAN)) {
			labTests = patientLabTestRepository.getPatientLabTestListWithCondition(requestData.getPatientTrackId(),
					patientVisitId, Constants.BOOLEAN_FALSE);
		} else {
			labTests = patientLabTestRepository.getPatientLabTestList(requestData.getPatientTrackId(), patientVisitId,
					Constants.BOOLEAN_FALSE);

		}
		response.setPatientLabTest(labTests);
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removePatientLabTest(GetRequestDTO requestData) {
		PatientLabTest patientLabTest = null;
		if (!Objects.isNull(requestData.getId())) {
			patientLabTest = patientLabTestRepository.findByIdAndIsDeleted(requestData.getId(),
					Constants.BOOLEAN_FALSE);
			if (Objects.isNull(patientLabTest)) {
				throw new DataNotFoundException(11004);
			}

			if (!Objects.isNull(patientLabTest.getResultDate())) {
				throw new DataNotAcceptableException(10011);
			}
			patientLabTest.setDeleted(true);
			patientLabTest = patientLabTestRepository.save(patientLabTest);
			List<PatientLabTest> patientLabTests = patientLabTestRepository
					.findAllByPatientVisitIdAndIsDeleted(patientLabTest.getPatientVisitId(), Constants.BOOLEAN_FALSE);

			if (0 == patientLabTests.size()) {
				PatientVisit patientVisit = patientVisitService.getPatientVisit(requestData.getPatientVisitId(),
						requestData.getTenantId());
				patientVisit.setInvestigation(false);
				patientVisitService.updatePatientVisit(patientVisit);

			}
			updatePatientTrackerLabtestReferral(patientLabTest.getPatientTrackId(), requestData.getTenantId(),
					Constants.BOOLEAN_FALSE);
		}
		return Constants.BOOLEAN_TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public int reviewPatientLabTest(GetRequestDTO requestData) {
		int noOfRowsAffected = 0;
		if (Objects.isNull(requestData.getPatientTrackId())) {
			throw new DataNotAcceptableException(10010);
		}
		if (!Objects.isNull(requestData.getId())) {
			noOfRowsAffected = patientLabTestRepository.updateIsReviewed(requestData.getId(), requestData.getTenantId(),
					requestData.getComment());
		}
		if (1 != noOfRowsAffected) {
			throw new DataNotFoundException(11004);
		}
		return noOfRowsAffected;
	}

	/**
	 * Constructs PatientLabTest data to insert into database.
	 *
	 * @param requestData request Data
	 * @return List of PatientLabTest Entity
	 * @author Niraimathi S
	 */
	private List<PatientLabTest> constructPatientLabTestData(PatientLabTestRequestDTO requestData) {
		List<PatientLabTest> patientLabTests = new ArrayList<PatientLabTest>();
		List<PatientLabTestDTO> patientLabTestDTOS = new ArrayList<>(requestData.getLabTest());
		for (PatientLabTestDTO patientLabTestDTO : patientLabTestDTOS) {
			PatientLabTest patientLabTest = new PatientLabTest();
			patientLabTest.setLabTestId(Long.parseLong(patientLabTestDTO.getLabTestId()));
			patientLabTest.setLabTestName(patientLabTestDTO.getLabTestName());
			patientLabTest.setResultDate(patientLabTestDTO.getResultDate());
			patientLabTest.setReferredBy(patientLabTestDTO.getReferredBy());
			patientLabTest.setIsReviewed(patientLabTestDTO.getIsReviewed());
			patientLabTest.setTenantId(patientLabTestDTO.getTenantId());
			patientLabTest.setPatientTrackId(patientLabTestDTO.getPatientTrackId());
			patientLabTest.setPatientVisitId(patientLabTestDTO.getPatientVisitId());
			patientLabTest.setResultUpdateBy(patientLabTestDTO.getResultUpdateBy());
			patientLabTest.setActive(Constants.BOOLEAN_TRUE);
			patientLabTest.setDeleted(Constants.BOOLEAN_FALSE);
			patientLabTests.add(patientLabTest);
		}
		return patientLabTests;
	}

	/**
	 * Gets other Labtest from the database based on countryId.
	 *
	 * @param data request data
	 * @return LabTest Entity
	 * @author Niraimathi S
	 */
	private LabTest getOtherLabTest(PatientLabTestRequestDTO data) {
		LabTest labTest = getLabTestbyName(Constants.OTHER, 1); // country id obtained from user information.
		if (Objects.isNull(labTest)) {
			throw new DataNotFoundException(10005);
		}
		return labTest;
	}

	/**
	 * Retrieves a LabTest entity based on its name and countryId from Admin service
	 * through an API call.
	 *
	 * @param searchTerm LabTest name
	 * @param countryId  Country id
	 * @return LabTest Entity.
	 * @author Niraimathi S
	 */
	private LabTest getLabTestbyName(String searchTerm, long countryId) {
		SearchRequestDTO requestEntity = new SearchRequestDTO();
		requestEntity.setCountryId(countryId);
		requestEntity.setSearchTerm(searchTerm);
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
//		HttpEntity<SearchRequestDTO> entity = new HttpEntity<SearchRequestDTO>(requestEntity, headers);
//		return restTemplate.exchange("http://192.168.13.155/admin-service/labtest/patient-labtest/get-by-name",
//				HttpMethod.POST, entity, LabTest.class).getBody();
		ResponseEntity<LabTest> labTest = apiInterface.getLabTestByName(requestEntity);
		return labTest.getBody();
	}

	/**
	 * Retrieves a list of LabTest entities based on a list of labTestIds from Admin
	 * service through an API call.
	 *
	 * @param labTestIds
	 * @return List of LabTest Entities
	 * @author Niraimathi S
	 */
	private List<LabTest> getLabTestsByIds(Set<Long> labTestIds) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
//		List<LabTest> labTests = restTemplate
//				.exchange("http://192.168.13.155/admin-service/labtest/patient-labtest/get-list-by-ids",
//						HttpMethod.POST, new HttpEntity<>(labTestIds, headers), List.class)
//				.getBody();
//		return labTests;

		ResponseEntity<List<LabTest>> list = apiInterface.getLabTestsByIds(labTestIds);
		return list.getBody();
	}

	/**
	 * Validates the request date to check null.
	 *
	 * @param requestData Request Data
	 * @author Niraimathi S
	 */
	private void validateRequestData(PatientLabTestRequestDTO requestData) {
		if (Objects.isNull(requestData)) {
			throw new BadRequestException(10008);
		}
		if (Objects.isNull(requestData.getLabTest()) && 1 > requestData.getLabTest().size()) {
			throw new BadRequestException(10009);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PatientLabTestResult> createPatientLabTestResult(PatientLabTestResultRequestDTO requestData) {
		if (Objects.isNull(requestData.getPatientLabTestId())) {
			throw new BadRequestException(11005);
		}

		if (Objects.isNull(requestData.getIsEmptyRanges()) || !requestData.getIsEmptyRanges()) {
			validatePatientLabTestResultRequest(requestData);
		}

		PatientLabTest patientLabTest = patientLabTestRepository.findByIdAndIsDeleted(requestData.getPatientLabTestId(),
				Constants.BOOLEAN_FALSE);

		if (Objects.isNull(patientLabTest)) {
			throw new DataNotFoundException(11004);
		}

		List<Long> labTestResultIds = getLabTestResultsByLabTestId(patientLabTest.getLabTestId());
		boolean isValidIds = labTestResultIds.containsAll(requestData.getPatientLabTestResults().stream()
				.map(result -> Long.parseLong(result.getId())).collect(Collectors.toList()));

		if (!isValidIds) {
			throw new DataNotAcceptableException(11007);
		}
		List<PatientLabTestResult> patientLabTestResults = constructPatientLabTestResults(requestData, patientLabTest);

		patientLabTestResults = patientLabTestResultRepository.saveAll(patientLabTestResults);

		updatePatientLabtestValues(requestData, patientLabTest);
		updatePatientTrackerLabtestReferral(patientLabTest.getPatientTrackId(), patientLabTest.getTenantId(),
				Constants.BOOLEAN_FALSE);
		return patientLabTestResults;
	}

	/**
	 * This method is used to construct PatientLabTestResult Data.
	 *
	 * @param requestData    Request data
	 * @param patientLabTest Patient LabTest Data
	 * @return List of Constructed PatientLabTestResult
	 * @author Niraimathi S
	 */
	private List<PatientLabTestResult> constructPatientLabTestResults(PatientLabTestResultRequestDTO requestData,
			PatientLabTest patientLabTest) {
		List<PatientLabTestResult> results = new ArrayList<>();
		PatientLabTestResult patientLabTestResult = null;
		for (PatientLabTestResultDTO result : requestData.getPatientLabTestResults()) {
			patientLabTestResult = new PatientLabTestResult();
			patientLabTestResult.setPatientLabTestId(requestData.getPatientLabTestId());
			patientLabTestResult.setLabTestId(patientLabTest.getLabTestId());
			patientLabTestResult.setPatientVisitId(patientLabTest.getPatientVisitId());
			patientLabTestResult.setPatientTrackId(patientLabTest.getPatientTrackId());
			patientLabTestResult.setDisplayName(result.getDisplayName());
			patientLabTestResult.setResultName(result.getName());
			patientLabTestResult.setUnit(result.getUnit());
			patientLabTestResult.setTenantId(requestData.getTenantId());
			patientLabTestResult.setIsAbnormal(
					Objects.isNull(result.getIsAbnormal()) ? Constants.BOOLEAN_FALSE : result.getIsAbnormal());
			patientLabTestResult.setPatientLabTestId(requestData.getPatientLabTestId());
			patientLabTestResult.setLabTestResultId(Long.parseLong(result.getId()));
			patientLabTestResult.setDisplayOrder(result.getDisplayOrder());
			patientLabTestResult.setResultStatus(result.getResultStatus());
			patientLabTestResult.setResultValue(result.getResultValue());
			results.add(patientLabTestResult);
		}
		return results;
	}

	/**
	 * Validates the request data for PatientLabTestResult.
	 *
	 * @param requestData Request Data
	 * @author Niraimathi S
	 */
	private void validatePatientLabTestResultRequest(PatientLabTestResultRequestDTO requestData) {
		if (requestData.getPatientLabTestResults().isEmpty()) {
			throw new BadRequestException(10008);
		}
		for (PatientLabTestResultDTO result : requestData.getPatientLabTestResults()) {
			if (Objects.isNull(result.getDisplayName()) || Objects.isNull(result.getResultStatus())
					|| Objects.isNull(result.getIsAbnormal())) {
				throw new BadRequestException(11006);
			}
		}
	}

	/**
	 * This method gets labtestResults using labtestId.
	 *
	 * @param labTestId Labtest Id
	 * @return List of LabTestResult Ids
	 * @author Niraimathi S
	 */
	private List<Long> getLabTestResultsByLabTestId(Long labTestId) {
		HttpHeaders headers = new HttpHeaders();
		ModelMapper modelMapper = new ModelMapper();
		headers.setContentType(MediaType.APPLICATION_JSON);
//		ResponseEntity<Map> userResponse = restTemplate.exchange(
//				"http://192.168.13.155/admin-service/labtest/labtest-result/{id}", HttpMethod.GET,
//				new HttpEntity<>(null, headers), Map.class, labTestId);
//		List<LabTestResult> response = modelMapper.map(userResponse.getBody().get("entity"),
//				new TypeToken<List<LabTestResult>>() {
//				}.getType());
//
//		return response.stream().map(BaseEntity::getId).collect(Collectors.toList());

		ResponseEntity<Map> userResponse = apiInterface.getLabTestResultsByLabTestId(labTestId);
		List<LabTestResult> response = modelMapper.map(userResponse.getBody().get("entity"),
				new TypeToken<List<LabTestResult>>() {
				}.getType());
		return response.stream().map(BaseEntity::getId).collect(Collectors.toList());
	}

	/**
	 * Used for updating the result date, comments, and is reviewed in
	 * patientLabTest.
	 *
	 * @param requestData    request data
	 * @param patientLabTest patient labtest data
	 * @author Niraimathi S
	 */
	private void updatePatientLabtestValues(PatientLabTestResultRequestDTO requestData, PatientLabTest patientLabTest) {
		patientLabTest.setResultDate(requestData.getTestedOn());
		patientLabTest.setIsReviewed(requestData.getIsReviewed());
		patientLabTest.setComments(requestData.getComment());
		patientLabTest.setTenantId(requestData.getTenantId());
// =======
//     private final RestTemplate restTemplate = new RestTemplate();

//     @Autowired
//     private PatientLabTestRepository patientLabTestRepository;

//     @Autowired
//     private PatientVisitService patientVisitService;

//     @Autowired
//     private PatientLabTestResultRepository patientLabTestResultRepository;

//     @Autowired
//     private PatientTrackerService patientTrackerService;

//     /**
//      * {@inheritDoc}
//      */
//     public List<PatientLabTest> createPatientLabTest(PatientLabTestRequestDTO requestData) {
//         validateRequestData(requestData);
//         boolean isOtherLabTest;
//         isOtherLabTest = requestData.getLabTest().stream()
//                 .anyMatch(data -> data.getLabTestId().equalsIgnoreCase(Constants.OTHER));

//         if (isOtherLabTest) {
//             LabTest labTest = getOtherLabTest(requestData);
//             requestData.getLabTest().forEach(entry -> {
//                 if (entry.getLabTestId().equalsIgnoreCase(Constants.OTHER)) {
//                     entry.setLabTestId(labTest.getId().toString());
//                 }
//             });
//         }
//         Set<String> uniqueLabTestNames = new HashSet<>();
//         Set<Long> uniqueLabTestIds = new HashSet<>();
//         if (!Objects.isNull(requestData.getLabTest())) {
//             for (PatientLabTestDTO test : requestData.getLabTest()) {
//                 test.setTenantId(requestData.getTenantId());
//                 test.setPatientTrackId(requestData.getPatientTrackId());
//                 test.setPatientVisitId(requestData.getPatientVisitId());
//                 test.setIsReviewed(test.getIsReviewed());
//                 uniqueLabTestNames.add(test.getLabTestName());
//                 uniqueLabTestIds.add(Long.parseLong(test.getLabTestId()));
//             }
//         }
//         if (uniqueLabTestNames.size() != requestData.getLabTest().size()) {
//             throw new BadRequestException(10006);
//         }
//         List<LabTest> labtests = getLabTestsByIds(uniqueLabTestIds);

//         if (uniqueLabTestIds.size() != labtests.size()) {
//             throw new DataNotFoundException(10007);
//         }

//         List<PatientLabTest> patientLabTests = constructPatientLabTestData(requestData);
//         patientLabTests = patientLabTestRepository.saveAll(patientLabTests);

//         patientVisitService.updateInvestigationStatus(Constants.BOOLEAN_TRUE, requestData.getPatientVisitId());
//         updatePatientTrackerLabtestReferral(requestData.getPatientTrackId(), requestData.getTenantId(),
//                 Constants.BOOLEAN_TRUE);
//         return patientLabTests;
//     }

//     /**
//      * {@inheritDoc}
//      */
//     public PatientLabTestResponseDTO getPatientLabTestList(GetRequestDTO requestData) {
//         if (Objects.isNull(requestData.getPatientTrackId())) {
//             throw new DataNotAcceptableException(10010);
//         }
//         Long patientVisitId = requestData.getPatientVisitId();
//         PatientLabTestResponseDTO response = new PatientLabTestResponseDTO();
//         List<PatientVisit> patientVisitList = new ArrayList<>();
//         List<PatientLabTest> labTests;

//         if (requestData.isLatestRequired() && Objects.isNull(requestData.getPatientVisitId())) {
//             patientVisitList =
//                     patientVisitService.getPatientVisitDates(requestData.getPatientTrackId(), Constants.BOOLEAN_TRUE,
//                             null, null);

//             patientVisitId =
//                     0 != patientVisitList.size() ? patientVisitList.get(patientVisitList.size() - 1).getId() : null;
//             response.setPatientLabtestDates(
//                     patientVisitList.stream().map(visit -> Map.of("id", visit.getId(), "date", visit.getVisitDate()))
//                             .collect(Collectors.toList()));
//             System.out.println(response.getPatientLabtestDates());
//         }
//         if (!Objects.isNull(requestData.getRoleName()) &&
//                 requestData.getRoleName().equals(Constants.ROLE_LAB_TECHNICIAN)) {
//             labTests = patientLabTestRepository.getPatientLabTestListWithCondition(requestData.getPatientTrackId(),
//                     patientVisitId, Constants.BOOLEAN_FALSE);
//         } else {
//             labTests = patientLabTestRepository.getPatientLabTestList(requestData.getPatientTrackId(), patientVisitId,
//                     Constants.BOOLEAN_FALSE);

//         }
//         response.setPatientLabTest(labTests);
//         return response;
//     }

//     /**
//      * {@inheritDoc}
//      */
//     public boolean removePatientLabTest(GetRequestDTO requestData) {
//         PatientLabTest patientLabTest = null;
//         if (!Objects.isNull(requestData.getId())) {
//             patientLabTest =
//                     patientLabTestRepository.findByIdAndIsDeleted(requestData.getId(), Constants.BOOLEAN_FALSE);
//             if (Objects.isNull(patientLabTest)) {
//                 throw new DataNotFoundException(11004);
//             }

//             if (!Objects.isNull(patientLabTest.getResultDate())) {
//                 throw new DataNotAcceptableException(10011);
//             }
//             patientLabTest.setDeleted(true);
//             patientLabTest = patientLabTestRepository.save(patientLabTest);
//             List<PatientLabTest> patientLabTests =
//                     patientLabTestRepository.findAllByPatientVisitIdAndIsDeleted(patientLabTest.getPatientVisitId(),
//                             Constants.BOOLEAN_FALSE);

//             if (0 == patientLabTests.size()) {
//                 patientVisitService.updateInvestigationStatus(Constants.BOOLEAN_FALSE,
//                         patientLabTest.getPatientVisitId());

//             }
//             updatePatientTrackerLabtestReferral(patientLabTest.getPatientTrackId(), requestData.getTenantId(),
//                     Constants.BOOLEAN_FALSE);
//         }
//         return Constants.BOOLEAN_TRUE;
//     }

//     /**
//      * {@inheritDoc}
//      */
//     public int reviewPatientLabTest(GetRequestDTO requestData) {
//         int noOfRowsAffected = 0;
//         if (Objects.isNull(requestData.getPatientTrackId())) {
//             throw new DataNotAcceptableException(10010);
//         }
//         if (!Objects.isNull(requestData.getId())) {
//             noOfRowsAffected = patientLabTestRepository.updateIsReviewed(requestData.getId(), requestData.getTenantId(),
//                     requestData.getComment());
//         }
//         if (1 != noOfRowsAffected) {
//             throw new DataNotFoundException(11004);
//         }
//         return noOfRowsAffected;
//     }

//     /**
//      * Constructs PatientLabTest data to insert into database.
//      *
//      * @param requestData request Data
//      * @return List of PatientLabTest Entity
//      * @author Niraimathi S
//      */
//     private List<PatientLabTest> constructPatientLabTestData(PatientLabTestRequestDTO requestData) {
//         List<PatientLabTest> patientLabTests = new ArrayList<PatientLabTest>();
//         List<PatientLabTestDTO> patientLabTestDTOS = new ArrayList<>(requestData.getLabTest());
//         for (PatientLabTestDTO patientLabTestDTO : patientLabTestDTOS) {
//             PatientLabTest patientLabTest = new PatientLabTest();
//             patientLabTest.setLabTestId(Long.parseLong(patientLabTestDTO.getLabTestId()));
//             patientLabTest.setLabTestName(patientLabTestDTO.getLabTestName());
//             patientLabTest.setResultDate(patientLabTestDTO.getResultDate());
//             patientLabTest.setReferredBy(patientLabTestDTO.getReferredBy());
//             patientLabTest.setIsReviewed(patientLabTestDTO.getIsReviewed());
//             patientLabTest.setTenantId(patientLabTestDTO.getTenantId());
//             patientLabTest.setPatientTrackId(patientLabTestDTO.getPatientTrackId());
//             patientLabTest.setPatientVisitId(patientLabTestDTO.getPatientVisitId());
//             patientLabTest.setResultUpdateBy(patientLabTestDTO.getResultUpdateBy());
//             patientLabTests.add(patientLabTest);
//         }
//         return patientLabTests;
//     }

//     /**
//      * Gets other Labtest from the database based on countryId.
//      *
//      * @param data request data
//      * @return LabTest Entity
//      * @author Niraimathi S
//      */
//     private LabTest getOtherLabTest(PatientLabTestRequestDTO data) {
//         LabTest labTest = getLabTestbyName(Constants.OTHER, 1); //country id obtained from user information.
//         if (Objects.isNull(labTest)) {
//             throw new DataNotFoundException(10005);
//         }
//         return labTest;
//     }

//     /**
//      * Retrieves a LabTest entity based on its name and countryId from Admin service through an API call.
//      *
//      * @param searchTerm LabTest name
//      * @param countryId  Country id
//      * @return LabTest Entity.
//      * @author Niraimathi S
//      */
//     private LabTest getLabTestbyName(String searchTerm, long countryId) {
//         SearchRequestDTO requestEntity = new SearchRequestDTO();
//         requestEntity.setCountryId(countryId);
//         requestEntity.setSearchTerm(searchTerm);
//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);
//         HttpEntity<SearchRequestDTO> entity = new HttpEntity<SearchRequestDTO>(requestEntity, headers);
//         return restTemplate.exchange("http://192.168.13.155/admin-service/labtest/patient-labtest/get-by-name",
//                 HttpMethod.POST, entity, LabTest.class).getBody();
//     }

//     /**
//      * Retrieves a list of LabTest entities based on a list of labTestIds from Admin service through an API call.
//      *
//      * @param labTestIds
//      * @return List of LabTest Entities
//      * @author Niraimathi S
//      */
//     private List<LabTest> getLabTestsByIds(Set<Long> labTestIds) {
//         HttpHeaders headers = new HttpHeaders();
//         headers.setContentType(MediaType.APPLICATION_JSON);
//         List<LabTest> labTests =
//                 restTemplate.exchange("http://192.168.13.155/admin-service/labtest/patient-labtest/get-list-by-ids",
//                         HttpMethod.POST, new HttpEntity<>(labTestIds, headers), List.class).getBody();
//         return labTests;
//     }

//     /**
//      * Validates the request date to check null.
//      *
//      * @param requestData Request Data
//      * @author Niraimathi S
//      */
//     private void validateRequestData(PatientLabTestRequestDTO requestData) {
//         if (Objects.isNull(requestData)) {
//             throw new BadRequestException(10008);
//         }
//         if (Objects.isNull(requestData.getLabTest()) && 1 > requestData.getLabTest().size()) {
//             throw new BadRequestException(10009);
//         }
//     }

//     /**
//      * {@inheritDoc}
//      */
//     public List<PatientLabTestResult> createPatientLabTestResult(PatientLabTestResultRequestDTO requestData) {
//         if (Objects.isNull(requestData.getPatientLabTestId())) {
//             throw new BadRequestException(11005);
//         }

//         if (Objects.isNull(requestData.getIsEmptyRanges()) || !requestData.getIsEmptyRanges()) {
//             validatePatientLabTestResultRequest(requestData);
//         }

//         PatientLabTest patientLabTest = patientLabTestRepository.findByIdAndIsDeleted(requestData.getPatientLabTestId(),
//                 Constants.BOOLEAN_FALSE);

//         if (Objects.isNull(patientLabTest)) {
//             throw new DataNotFoundException(11004);
//         }

//         List<Long> labTestResultIds = getLabTestResultsByLabTestId(patientLabTest.getLabTestId());
//         System.out.println("+++++++++++++++++++++++labtestresult ids" + labTestResultIds);
//         boolean isValidIds = labTestResultIds.containsAll(
//                 requestData.getPatientLabTestResults().stream().map(result -> Long.parseLong(result.getId()))
//                         .collect(Collectors.toList()));

//         if (!isValidIds) {
//             throw new DataNotAcceptableException(11007);
//         }
//         List<PatientLabTestResult> patientLabTestResults = constructPatientLabTestResults(requestData, patientLabTest);

//         patientLabTestResults = patientLabTestResultRepository.saveAll(patientLabTestResults);

//         updatePatientLabtestValues(requestData, patientLabTest);
//         updatePatientTrackerLabtestReferral(patientLabTest.getPatientTrackId(), patientLabTest.getTenantId(),
//                 Constants.BOOLEAN_FALSE);
//         return patientLabTestResults;
//     }

//     /**
//      * This method is used to construct PatientLabTestResult Data.
//      *
//      * @param requestData    Request data
//      * @param patientLabTest Patient LabTest Data
//      * @return List of Constructed PatientLabTestResult
//      * @author Niraimathi S
//      */
//     private List<PatientLabTestResult> constructPatientLabTestResults(PatientLabTestResultRequestDTO requestData, PatientLabTest patientLabTest) {
//         List<PatientLabTestResult> results = new ArrayList<>();
//         PatientLabTestResult patientLabTestResult = null;
//         for (PatientLabTestResultDTO result : requestData.getPatientLabTestResults()) {
//             patientLabTestResult = new PatientLabTestResult();
//             patientLabTestResult.setPatientLabTestId(requestData.getPatientLabTestId());
//             patientLabTestResult.setLabTestId(patientLabTest.getLabTestId());
//             patientLabTestResult.setPatientVisitId(patientLabTest.getPatientVisitId());
//             patientLabTestResult.setPatientTrackId(patientLabTest.getPatientTrackId());
//             patientLabTestResult.setDisplayName(result.getDisplayName());
//             patientLabTestResult.setResultName(result.getName());
//             patientLabTestResult.setUnit(result.getUnit());
//             patientLabTestResult.setTenantId(requestData.getTenantId());
//             patientLabTestResult.setIsAbnormal(
//                     Objects.isNull(result.getIsAbnormal()) ? Constants.BOOLEAN_FALSE : result.getIsAbnormal());
//             patientLabTestResult.setPatientLabTestId(requestData.getPatientLabTestId());
//             patientLabTestResult.setLabTestResultId(Long.parseLong(result.getId()));
//             patientLabTestResult.setDisplayOrder(result.getDisplayOrder());
//             patientLabTestResult.setResultStatus(result.getResultStatus());
//             patientLabTestResult.setResultValue(result.getResultValue());
//             results.add(patientLabTestResult);
//         }
//         return results;
//     }

//     /**
//      * Validates the request data for PatientLabTestResult.
//      *
//      * @param requestData Request Data
//      * @author Niraimathi S
//      */
//     private void validatePatientLabTestResultRequest(PatientLabTestResultRequestDTO requestData) {
//         if (requestData.getPatientLabTestResults().isEmpty()) {
//             throw new BadRequestException(10008);
//         }
//         for (PatientLabTestResultDTO result : requestData.getPatientLabTestResults()) {
//             if (Objects.isNull(result.getDisplayName()) || Objects.isNull(result.getResultStatus()) ||
//                     Objects.isNull(result.getIsAbnormal())) {
//                 throw new BadRequestException(11006);
//             }
//         }
//     }

//     /**
//      * This method gets labtestResults using labtestId.
//      *
//      * @param labTestId Labtest Id
//      * @return List of LabTestResult Ids
//      * @author Niraimathi S
//      */
//     private List<Long> getLabTestResultsByLabTestId(Long labTestId) {
//         HttpHeaders headers = new HttpHeaders();
//         ModelMapper modelMapper = new ModelMapper();
//         headers.setContentType(MediaType.APPLICATION_JSON);
//         ResponseEntity<Map> userResponse =
//                 restTemplate.exchange("http://192.168.13.155/admin-service/labtest/labtest-result/{id}", HttpMethod.GET,
//                         new HttpEntity<>(null, headers), Map.class, labTestId);
//         List<LabTestResult> response =
//                 modelMapper.map(userResponse.getBody().get("entity"), new TypeToken<List<LabTestResult>>() {
//                 }.getType());

//         return response.stream().map(BaseEntity::getId).collect(Collectors.toList());
//     }

//     /**
//      * Used for updating the result date, comments, and is reviewed in patientLabTest.
//      *
//      * @param requestData    request data
//      * @param patientLabTest patient labtest data
//      * @author Niraimathi S
//      */
//     private void updatePatientLabtestValues(PatientLabTestResultRequestDTO requestData, PatientLabTest patientLabTest) {
//         patientLabTest.setResultDate(requestData.getTestedOn());
//         patientLabTest.setIsReviewed(requestData.getIsReviewed());
//         patientLabTest.setComments(requestData.getComment());
//         patientLabTest.setTenantId(requestData.getTenantId());
// >>>>>>> Stashed changes
//        TODO: set user id and isAbnormal
//        result_update_by: params.user._id,
//        is_abnormal:data.is_abnormal
		patientLabTestRepository.save(patientLabTest);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatientLabTestResultResponseDTO getPatientLabTestResults(PatientLabTestResultRequestDTO requestData) {
		PatientLabTestResultResponseDTO response = new PatientLabTestResultResponseDTO();
		if (Objects.isNull(requestData.getPatientLabTestId())) {
			throw new BadRequestException(11008);
		}
		PatientLabTest patientLabTest = patientLabTestRepository.findByIdAndIsDeleted(requestData.getPatientLabTestId(),
				Constants.BOOLEAN_FALSE);

		if (!Objects.isNull(patientLabTest)) {
			List<PatientLabTestResult> results = patientLabTestResultRepository
					.findAllByPatientLabTestIdAndIsDeletedAndTenantId(requestData.getPatientLabTestId(),
							Constants.BOOLEAN_FALSE, requestData.getTenantId());
			if (!results.isEmpty()) {
				response.setPatientLabTestResults(results);
				response.setResultDate(patientLabTest.getResultDate());
				response.setComment(patientLabTest.getComments());
			}
		}
		return response;
	}

	/**
	 * Updates the isLabTestReferred field if a labtest referred to a patient in
	 * PatientTracker.
	 *
	 * @param patientTrackId    PatientTracker Id
	 * @param tenantId          tenantId
	 * @param isLabTestReferred boolean value isLabTestReferred
	 * @author Niraimathi S
	 */
	private void updatePatientTrackerLabtestReferral(long patientTrackId, Long tenantId, boolean isLabTestReferred) {
		if (!isLabTestReferred) {
			List<PatientLabTest> labTests = patientLabTestRepository.getPatientLabTestsWithoutResults(patientTrackId,
					tenantId);
			if (labTests.isEmpty()) {
//            patientTrackerService.addOrUpdatePatientTracker()
				patientTrackerService.updatePatientTrackerLabtestReferral(patientTrackId, tenantId,
						Constants.BOOLEAN_FALSE);
			}
		} else {
			patientTrackerService.updatePatientTrackerLabtestReferral(patientTrackId, tenantId, Constants.BOOLEAN_TRUE);
		}
	}

	public List<PatientLabTest> getPatientLabTest(Long patientTrackId, Long patientVisitId) {
		List<PatientLabTest> patientLabTests = patientLabTestRepository.getPatientLabTestList(patientTrackId,
				patientVisitId, Constants.BOOLEAN_FALSE);
		return patientLabTests.isEmpty() ? new ArrayList<>() : patientLabTests;
	}

	public int getLabtestCount(Long patientTrackId) {
		return patientLabTestRepository.getLabTestNoReviewedCount(patientTrackId);
	}
}