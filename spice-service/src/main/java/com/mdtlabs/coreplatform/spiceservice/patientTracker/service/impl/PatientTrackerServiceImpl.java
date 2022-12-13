package com.mdtlabs.coreplatform.spiceservice.patientTracker.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.exception.SpiceValidation;
import com.mdtlabs.coreplatform.common.logger.SpiceLogger;
import com.mdtlabs.coreplatform.common.model.dto.spice.ConfirmDiagnosisDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.MyPatientListDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientFilterDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientSortDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchPatientListDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.common.util.CommonUtil;
import com.mdtlabs.coreplatform.common.util.Pagination;
import com.mdtlabs.coreplatform.spiceservice.mentalhealth.repository.MentalHealthRepository;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.repository.PatientTrackerRepository;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;

@Service
public class PatientTrackerServiceImpl implements PatientTrackerService {
	ModelMapper modelMapper = new ModelMapper();

	@Autowired
	private PatientTrackerRepository patientTrackerRepository;

	@Autowired
	private MentalHealthRepository mentalHealthRepository;

	/**
	 * {@inheritDoc}
	 */
	public PatientTracker addOrUpdatePatientTracker(PatientTracker patientTracker) {
		if (Objects.isNull(patientTracker)) {
			throw new BadRequestException(1000);
		} else {
			if (!Objects.isNull(patientTracker.getPhoneNumber())
					&& !CommonUtil.validatePhoneNumber(patientTracker.getPhoneNumber())) {
				throw new SpiceValidation(00002);
			}
			return patientTrackerRepository.save(patientTracker);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public PatientTracker getPatientTrackerByNationalId(String nationalId) {
		return patientTrackerRepository.findByNationalId(nationalId);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatientTracker getPatientTrackerById(long patientTrackerId) {
		PatientTracker patientTracker = patientTrackerRepository.findByIdAndIsDeleted(patientTrackerId, false);
		if (Objects.isNull(patientTracker)) {
			throw new DataNotFoundException(4004);
		} else {
			return patientTracker;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<MyPatientListDTO> listMyPatients(PatientRequestDTO patientRequestDTO) {

		List<MyPatientListDTO> patientListDTO = new ArrayList<>();

		Pageable pageable = getSortingForPatients(patientRequestDTO.getPatientSortDTO());

		HashMap<String, String> filterMap = new HashMap<>();

		if (!Objects.isNull(patientRequestDTO.getPatientFilterDTO())) {
			filterMap = getFiltersForPatients(patientRequestDTO);
		}

		Page<PatientTracker> patientTrackerList = patientTrackerRepository.getPatientsListWithPagination(
				filterMap.get("medicalReviewStartDate"), filterMap.get("medicalReviewEndDate"),
				filterMap.get("assessmentStartDate"), filterMap.get("assessmentEndDate"),
				Boolean.parseBoolean(filterMap.get("medicalReviewDate")),
				Boolean.parseBoolean(filterMap.get("assessmentDate")),
				patientRequestDTO.getPatientFilterDTO().getIsRedRiskPatient(),
				patientRequestDTO.getPatientFilterDTO().getCvdRiskLevel(),
				patientRequestDTO.getPatientFilterDTO().getScreeningReferral(),
				filterMap.get("patientStatusNotScreened"), filterMap.get("patientStatusEnrolled"),
				filterMap.get("patientStatusNotEnrolled"), pageable);
		List<PatientTracker> patientList = patientTrackerList.stream().collect(Collectors.toList());
		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		MyPatientListDTO patientDTO = null;
		for (PatientTracker patientTracker : patientList) {
			patientDTO = new MyPatientListDTO();
			modelMapper.map(patientTracker, patientDTO);
			if (!Objects.isNull(patientTracker.getEnrollmentAt())) {
				patientDTO.setAge(patientDTO.getAge()
						+ CommonUtil.calculatePatientAge(patientDTO.getAge(), patientTracker.getEnrollmentAt()));
			} else if (!Objects.isNull(patientTracker.getCreatedAt())) {
				patientDTO.setAge(patientDTO.getAge()
						+ CommonUtil.calculatePatientAge(patientDTO.getAge(), patientTracker.getCreatedAt()));
			}
			patientListDTO.add(patientDTO);
		}
		return patientListDTO;

	}

	/**
	 * {@inheritDoc}
	 */
	public List<SearchPatientListDTO> searchPatients(PatientRequestDTO patientRequestDTO) {

		List<SearchPatientListDTO> patientListDTO = new ArrayList<>();
		if (Objects.isNull(patientRequestDTO.getSearchId()) || StringUtils.isEmpty(patientRequestDTO.getSearchId())) {
			throw new DataNotAcceptableException(4005);
		}

		if (CommonUtil.validatePatientSearchData(Arrays.asList(patientRequestDTO.getSearchId()))) {
			return new ArrayList<>();
		}

		String nationalId = patientRequestDTO.getSearchId();
		String programId = patientRequestDTO.getSearchId().matches("[0-9]+")
				? patientRequestDTO.getSearchId().toLowerCase()
				: null;
		if (Objects.isNull(programId)) {
			nationalId = patientRequestDTO.getSearchId().toLowerCase();
		}
		Long tenantId = (!Objects.isNull(patientRequestDTO.getIsSearchUserOrgPatient())
				&& patientRequestDTO.getIsSearchUserOrgPatient()) ? patientRequestDTO.getTenantId() : null;

		Pageable pageable = getSortingForPatients(patientRequestDTO.getPatientSortDTO());

		HashMap<String, String> filterMap = new HashMap<>();

		if (!Objects.isNull(patientRequestDTO.getPatientFilterDTO())) {
			filterMap = getFiltersForPatients(patientRequestDTO);
		}

		Page<PatientTracker> patientTrackerList = patientTrackerRepository.searchPatientsWithPagination(tenantId,
				patientRequestDTO.getOperatingUnitId(), filterMap.get("medicalReviewStartDate"),
				filterMap.get("medicalReviewEndDate"), filterMap.get("assessmentStartDate"),
				filterMap.get("assessmentEndDate"), Boolean.parseBoolean(filterMap.get("medicalReviewDate")),
				Boolean.parseBoolean(filterMap.get("assessmentDate")),
				(!filterMap.isEmpty() ? patientRequestDTO.getPatientFilterDTO().getIsRedRiskPatient() : null),
				(!filterMap.isEmpty() ? patientRequestDTO.getPatientFilterDTO().getCvdRiskLevel() : null),
				(!filterMap.isEmpty() ? patientRequestDTO.getPatientFilterDTO().getScreeningReferral() : null),
				patientRequestDTO.getIsLabtestReferred(), patientRequestDTO.getIsMedicationPrescribed(),
				filterMap.get("patientStatusNotScreened"), filterMap.get("patientStatusEnrolled"),
				filterMap.get("patientStatusNotEnrolled"), nationalId, programId, pageable);
		List<PatientTracker> patientList = patientTrackerList.stream().collect(Collectors.toList());
		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		SearchPatientListDTO patientDTO = null;
		for (PatientTracker patientTracker : patientList) {
			patientDTO = new SearchPatientListDTO();
			modelMapper.map(patientTracker, patientDTO);
			if (!Objects.isNull(patientTracker.getEnrollmentAt())) {
				patientDTO.setAge(patientDTO.getAge()
						+ CommonUtil.calculatePatientAge(patientDTO.getAge(), patientTracker.getEnrollmentAt()));
			} else if (!Objects.isNull(patientTracker.getCreatedAt())) {
				patientDTO.setAge(patientDTO.getAge()
						+ CommonUtil.calculatePatientAge(patientDTO.getAge(), patientTracker.getCreatedAt()));
			}
			patientListDTO.add(patientDTO);
		}
		return patientListDTO;

	}

	/**
	 * Sorting conditions for patient list
	 * 
	 * @param patientSortDTO
	 * @return Pageable
	 */
	public Pageable getSortingForPatients(PatientSortDTO patientSortDTO) {

		Pageable pageable = null;

		if (!Objects.isNull(patientSortDTO)) {
			List<Sort.Order> sorts = new ArrayList<>();
			if (!Objects.isNull(patientSortDTO.getCvdRiskScoreAsc())) {
				if (patientSortDTO.getCvdRiskScoreAsc()) {
					sorts.add(new Sort.Order(Sort.Direction.ASC, FieldConstants.CVD_RISK_SCORE));
				} else {
					sorts.add(new Sort.Order(Sort.Direction.DESC, FieldConstants.CVD_RISK_SCORE));
				}
			}

			if (!Objects.isNull(patientSortDTO.getIsRedRiskPatientAsc())) {
				if (patientSortDTO.getIsRedRiskPatientAsc()) {
					sorts.add(new Sort.Order(Sort.Direction.ASC, FieldConstants.IS_RED_RISK_PATIENT));
				} else {
					sorts.add(new Sort.Order(Sort.Direction.DESC, FieldConstants.IS_RED_RISK_PATIENT));
				}
			}

			if (!Objects.isNull(patientSortDTO.getNextMedicalReviewDateAsc())) {
				if (patientSortDTO.getNextMedicalReviewDateAsc()) {
					sorts.add(new Sort.Order(Sort.Direction.ASC, FieldConstants.NEXT_MEDICAL_REVIEW_DATE));
				} else {
					sorts.add(new Sort.Order(Sort.Direction.DESC, FieldConstants.NEXT_MEDICAL_REVIEW_DATE));
				}
			}

			if (!Objects.isNull(patientSortDTO.getNextBpAssessmentDateAsc())) {
				if (patientSortDTO.getNextBpAssessmentDateAsc()) {
					sorts.add(new Sort.Order(Sort.Direction.ASC, FieldConstants.NEXT_BP_ASSESSMENT_DATE));
				} else {
					sorts.add(new Sort.Order(Sort.Direction.DESC, FieldConstants.NEXT_BP_ASSESSMENT_DATE));
				}
			}

			if (sorts.isEmpty()) {
				sorts.add(new Sort.Order(Sort.Direction.DESC, FieldConstants.UPDATED_AT));
			}
			pageable = Pagination.setPagination(patientSortDTO.getPageNumber(), patientSortDTO.getLimit(), sorts);
			return pageable;
		}

		pageable = Pagination.setPagination(0, 0, FieldConstants.UPDATED_AT, false);
		return pageable;

	}

	/**
	 * Filter conditions for patients
	 * 
	 * @param patientRequestDTO
	 * @return HashMap
	 */
	public HashMap<String, String> getFiltersForPatients(PatientRequestDTO patientRequestDTO) {

		HashMap<String, String> map = new HashMap<>();

		map.put("medicalReviewDate", "false");
		map.put("assessmentDate", "false");

		PatientFilterDTO patientFilterDTO = patientRequestDTO.getPatientFilterDTO();

		if (!Objects.isNull(patientFilterDTO)) {

			HashMap<String, String> datesMap = getTodayAndTomorrowDate();

			if (!Objects.isNull(patientFilterDTO.getScreeningReferral()) && patientFilterDTO.getScreeningReferral()) {
				map.put("patientStatusNotEnrolled", "SCREENED");
			}

			if (!Objects.isNull(patientFilterDTO.getPatientStatus())
					&& !patientFilterDTO.getPatientStatus().isBlank()) {
				if (patientFilterDTO.getPatientStatus().equalsIgnoreCase("enrolled")) {
					map.put("patientStatusEnrolled", "ENROLLED");
				} else {
					map.put("patientStatusNotEnrolled", "ENROLLED");
				}
			}

			if (!Objects.isNull(patientFilterDTO.getMedicalReviewDate())) {
				if (patientFilterDTO.getMedicalReviewDate().equalsIgnoreCase("today")) {
					map.put("medicalReviewStartDate", datesMap.get("todayStartDate"));
					map.put("medicalReviewEndDate", datesMap.get("todayEndDate"));
				}
				if (patientFilterDTO.getMedicalReviewDate().equalsIgnoreCase("tomorrow")) {
					map.put("medicalReviewStartDate", datesMap.get("tomorrowStartDate"));
					map.put("medicalReviewEndDate", datesMap.get("tomorrowEndDate"));
				}
			}

			if (!Objects.isNull(patientFilterDTO.getAssessmentDate())) {
				if (patientFilterDTO.getAssessmentDate().equalsIgnoreCase("today")) {
					map.put("assessmentStartDate", datesMap.get("todayStartDate"));
					map.put("assessmentEndDate", datesMap.get("todayEndDate"));
				}
				if (patientFilterDTO.getAssessmentDate().equalsIgnoreCase("tomorrow")) {
					map.put("assessmentStartDate", datesMap.get("tomorrowStartDate"));
					map.put("assessmentEndDate", datesMap.get("tomorrowEndDate"));
				}
			}

			if (!Objects.isNull(patientRequestDTO.getPatientSortDTO())
					&& !Objects.isNull(patientRequestDTO.getPatientSortDTO().getNextMedicalReviewDateAsc())
					&& Objects.isNull(map.get("medicalReviewStartDate"))
					&& Objects.isNull(map.get("medicalReviewEndDate"))) {
				map.put("medicalReviewDate", "true");
			}

			if (!Objects.isNull(patientRequestDTO.getPatientSortDTO())
					&& !Objects.isNull(patientRequestDTO.getPatientSortDTO().getNextBpAssessmentDateAsc())
					&& Objects.isNull(map.get("assessmentStartDate")) && Objects.isNull(map.get("assessmentEndDate"))) {
				map.put("assessmentDate", "true");
			}

		}

		return map;
	}

	/**
	 * Get today and tomorrow date in UTC time zone
	 * 
	 * @return HashMap
	 */
	public HashMap<String, String> getTodayAndTomorrowDate() {

		HashMap<String, String> datesMap = new HashMap<>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx");
		// Today date
		LocalDate today = LocalDate.now();

		// Start date time of today
		ZonedDateTime zonedDateTime = today.atStartOfDay(ZoneId.systemDefault());
		ZonedDateTime zonedDateTimeUTC = zonedDateTime.withZoneSameInstant(ZoneId.of(Constants.TIMEZONE_UTC));
		datesMap.put("todayStartDate", zonedDateTimeUTC.format(formatter));

		// End date time of today
		zonedDateTime = zonedDateTime.plusDays(1).minusSeconds(1);
		zonedDateTimeUTC = zonedDateTime.withZoneSameInstant(ZoneId.of(Constants.TIMEZONE_UTC));
		datesMap.put("todayEndDate", zonedDateTimeUTC.format(formatter));

		// Tomorrow date
		LocalDate tomorrow = today.plusDays(1);
		SpiceLogger.logInfo("tomorrowDate: " + tomorrow);

		// Start date time of tomorrow
		zonedDateTime = tomorrow.atStartOfDay(ZoneId.systemDefault());
		zonedDateTimeUTC = zonedDateTime.withZoneSameInstant(ZoneId.of(Constants.TIMEZONE_UTC));
		datesMap.put("tomorrowStartDate", zonedDateTimeUTC.format(formatter));

		// End date time of tomorrow
		zonedDateTime = zonedDateTime.plusDays(1).minusSeconds(1);
		zonedDateTimeUTC = zonedDateTime.withZoneSameInstant(ZoneId.of(Constants.TIMEZONE_UTC));
		datesMap.put("tomorrowEndDate", zonedDateTimeUTC.format(formatter));
		SpiceLogger.logInfo("dates json: " + datesMap);

		return datesMap;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<MyPatientListDTO> patientAdvanceSearch(PatientRequestDTO patientRequestDTO) {

		List<MyPatientListDTO> patientListDTO = new ArrayList<>();

		if (Objects.isNull(patientRequestDTO.getFirstName()) && Objects.isNull(patientRequestDTO.getLastName())
				&& Objects.isNull(patientRequestDTO.getPhoneNumber())) {
			throw new DataNotAcceptableException(4006);
		}

		if (!CommonUtil.validatePhoneNumber(patientRequestDTO.getPhoneNumber())) {
			throw new SpiceValidation(00002);
		}

		boolean isInvalidData = CommonUtil.validatePatientSearchData(Arrays.asList(patientRequestDTO.getFirstName(),
				patientRequestDTO.getLastName(), patientRequestDTO.getPhoneNumber()));

		if (isInvalidData) {
			return new ArrayList<>();
		}

		Pageable pageable = getSortingForPatients(patientRequestDTO.getPatientSortDTO());

		HashMap<String, String> filterMap = new HashMap<>();

		if (!Objects.isNull(patientRequestDTO.getPatientFilterDTO())) {
			filterMap = getFiltersForPatients(patientRequestDTO);
		}

		// TODO:: isGlobally condition

		Page<PatientTracker> patientTrackerList = patientTrackerRepository.getPatientsWithAdvanceSearch(
				patientRequestDTO.getFirstName(), patientRequestDTO.getLastName(), patientRequestDTO.getPhoneNumber(),
				filterMap.get("medicalReviewStartDate"), filterMap.get("medicalReviewEndDate"),
				filterMap.get("assessmentStartDate"), filterMap.get("assessmentEndDate"),
				Boolean.parseBoolean(filterMap.get("medicalReviewDate")),
				Boolean.parseBoolean(filterMap.get("assessmentDate")),
				(!filterMap.isEmpty() ? patientRequestDTO.getPatientFilterDTO().getIsRedRiskPatient() : null),
				(!filterMap.isEmpty() ? patientRequestDTO.getPatientFilterDTO().getCvdRiskLevel() : null),
				(!filterMap.isEmpty() ? patientRequestDTO.getPatientFilterDTO().getScreeningReferral() : null),
				filterMap.get("patientStatusNotScreened"), filterMap.get("patientStatusEnrolled"),
				filterMap.get("patientStatusNotEnrolled"), patientRequestDTO.getIsLabtestReferred(),
				patientRequestDTO.getIsMedicationPrescribed(), Constants.BOOLEAN_FALSE, pageable);

		List<PatientTracker> patientList = patientTrackerList.stream().collect(Collectors.toList());
		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		MyPatientListDTO patientDTO = null;
		for (PatientTracker patientTracker : patientList) {
			patientDTO = new MyPatientListDTO();
			modelMapper.map(patientTracker, patientDTO);
			if (!Objects.isNull(patientTracker.getEnrollmentAt())) {
				patientDTO.setAge(patientDTO.getAge()
						+ CommonUtil.calculatePatientAge(patientDTO.getAge(), patientTracker.getEnrollmentAt()));
			} else if (!Objects.isNull(patientTracker.getCreatedAt())) {
				patientDTO.setAge(patientDTO.getAge()
						+ CommonUtil.calculatePatientAge(patientDTO.getAge(), patientTracker.getCreatedAt()));
			}
			patientListDTO.add(patientDTO);
		}

		return patientListDTO;

	}

	/**
	 * {@inheritDoc}
	 */
	public void updatePatientTrackerLabtestReferral(long patientTrackId, Long tenantId, boolean isLabTestReferred) {
		patientTrackerRepository.updatePatientTrackerLabtestReferral(patientTrackId, tenantId, isLabTestReferred);
	}

	// public void setPHQ4Score(PatientTracker patientTracker, MentalHealthDTO
	// mentalHealth) {
	// patientTracker.setPhq4FirstScore(Constants.ZERO);
	// patientTracker.setPhq4SecondScore(Constants.ZERO);
	// if (!Objects.isNull(mentalHealth)) {
	// for (MentalHealthDetails mentalHealthDetails :
	// mentalHealth.getPhq4MentalHealth()) {
	// if (mentalHealthDetails.getDisplayOrder() == Constants.ONE
	// || mentalHealthDetails.getDisplayOrder() == Constants.TWO) {
	// patientTracker
	// .setPhq4FirstScore(patientTracker.getPhq4FirstScore() +
	// mentalHealthDetails.getScore());
	// } else if (mentalHealthDetails.getDisplayOrder() == Constants.THREE
	// || mentalHealthDetails.getDisplayOrder() == Constants.FOUR) {
	// patientTracker
	// .setPhq4SecondScore(patientTracker.getPhq4SecondScore() +
	// mentalHealthDetails.getScore());
	// }
	// }
	// }
	// }

	// public MentalHealth createMentalHealth(MentalHealth mentalHealth,
	// PatientTracker patientTracker) {
	// updateLatestMentalHealth(patientTracker);
	// // mentalHealth.setPatientTracker(patientTracker);
	// mentalHealth.setIsLatest(true);
	// return mentalHealthRepository.save(mentalHealth);
	// }

	// private void updateLatestMentalHealth(PatientTracker patientTracker) {
	// // MentalHealth mentalHealth =
	// mentalHealthRepository.findByPatientTrackerAndIsLatest(patientTracker, true);
	// // if (!Objects.isNull(mentalHealth)) {
	// // mentalHealth.setIsLatest(false);
	// // mentalHealthRepository.save(mentalHealth);
	// // }
	// }

	@Override
	public void UpdatePatientTrackerForBpLog(long patientTrackerId, BpLog bpLog, Date nextBpAssessmentDate) {
		PatientTracker patientTracker = patientTrackerRepository.findById(patientTrackerId).get();
		if (!Objects.isNull(bpLog.getHeight())) {
			patientTracker.setHeight(bpLog.getHeight());
		}
		if (!Objects.isNull(bpLog.getWeight())) {
			patientTracker.setWeight(bpLog.getWeight());
		}
		if (!Objects.isNull(bpLog.getBmi())) {
			patientTracker.setBmi(bpLog.getBmi());
		}
		if (!Objects.isNull(bpLog.getAvgSystolic())) {
			patientTracker.setAvgSystolic(bpLog.getAvgSystolic());
		}
		if (!Objects.isNull(bpLog.getAvgDiastolic())) {
			patientTracker.setAvgDiastolic(bpLog.getAvgDiastolic());
		}
		if (!Objects.isNull(bpLog.getAvgPulse())) {
			patientTracker.setAvgPulse(bpLog.getAvgPulse());
		}
		if (!Objects.isNull(bpLog.getCvdRiskLevel())) {
			patientTracker.setCvdRiskLevel(bpLog.getCvdRiskLevel());
		}
		if (!Objects.isNull(bpLog.getCvdRiskScore())) {
			patientTracker.setCvdRiskScore(bpLog.getCvdRiskScore());
		}
		if (!Objects.isNull(nextBpAssessmentDate)) {
			patientTracker.setNextBpAssessmentDate(nextBpAssessmentDate);
		}
		patientTracker.setLastAssessmentDate(new Date());
		if (!Objects.isNull(bpLog.getRiskLevel()) && !bpLog.getRiskLevel().isBlank()) {
			patientTracker.setRiskLevel(bpLog.getRiskLevel());
		}
		patientTracker.setTenantId(bpLog.getTenantId());
		patientTracker.setRedRiskPatient(bpLog.isRedRiskPatient());
		patientTrackerRepository.save(patientTracker);
	}

	@Override
	public void UpdatePatientTrackerForGlucoseLog(long patientTrackerId, GlucoseLog glucoseLog,
			Date nextBgAssessmentDate) {
		PatientTracker patientTracker = patientTrackerRepository.findById(patientTrackerId).get();
		if (!Objects.isNull(glucoseLog.getGlucoseValue()) && !Objects.isNull(glucoseLog.getGlucoseType())
				&& !Objects.isNull(glucoseLog.getGlucoseUnit())) {
			patientTracker.setGlucoseType(glucoseLog.getGlucoseType());
			patientTracker.setGlucoseUnit(glucoseLog.getGlucoseUnit());
			patientTracker.setGlucoseValue(glucoseLog.getGlucoseValue());
		}
		if (!Objects.isNull(nextBgAssessmentDate)) {
			patientTracker.setNextBgAssessmentDate(nextBgAssessmentDate);
		}
		patientTrackerRepository.save(patientTracker);

		// patientTrackerRepository.updatePatientTrackerForGlucoseLog(glucoseLog.getGlucoseValue(),
		// glucoseLog.getGlucoseUnit(), glucoseLog.getGlucoseType(),
		// nextBgAssessmentDate,patientTrackerId);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatientTracker findByNationalIdIgnoreCase(String nationalId) {
		return patientTrackerRepository.findByNationalIdIgnoreCase(nationalId);
	}

	public void updateRedRiskPatientStatus(long patientTrackerId, boolean status) {
		patientTrackerRepository.updateRedRiskPatientStatus(patientTrackerId, status);
	}

	/**
	 * {@inheritDoc}
	 */
	public ConfirmDiagnosisDTO updateConfirmDiagnosis(ConfirmDiagnosisDTO confirmDiagnosis) {

		PatientTracker patientTracker = getPatientTrackerById(confirmDiagnosis.getPatientTrackId());
		patientTracker.setConfirmDiagnosis(confirmDiagnosis.getConfirmDiagnosis());
		patientTracker.setDiagnosisComments(confirmDiagnosis.getDiagnosisComments());
		patientTracker.setIsConfirmDiagnosis(Constants.BOOLEAN_TRUE);
		// TODO: tenent id update
		patientTrackerRepository.save(patientTracker);
		confirmDiagnosis.setConfirmDiagnosis(true);
		return confirmDiagnosis;
	}

	/**
	 * Update fill prescription details in patienttracker
	 *
	 * @param id                     PatientTrackId
	 * @param isMedicationPrescribed isMedicationPrescribed field
	 * @author Niraimathi S
	 */
	public void updateForFillPrescription(Long id, boolean isMedicationPrescribed, Date lastAssessmentDate,
			Date nextMedicalReviewDate) {

		PatientTracker patientTracker = patientTrackerRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException(4004));
		patientTracker.setMedicationPrescribed(isMedicationPrescribed);
		if (!Objects.isNull(lastAssessmentDate)) {
			patientTracker.setLastAssessmentDate(lastAssessmentDate);
		}
		if (!Objects.isNull(nextMedicalReviewDate)) {
			patientTracker.setNextMedicalReviewDate(nextMedicalReviewDate);
		}
//		patientTrackerRepository.updateForFillPrescription(id, isMedicationPrescribed);
		patientTrackerRepository.save(patientTracker);
	}
}
