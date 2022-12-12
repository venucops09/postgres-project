package com.mdtlabs.coreplatform.spiceservice.screeningLog.service.impl;

import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.UnitConstants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.SpiceValidation;
import com.mdtlabs.coreplatform.common.model.dto.spice.GlucoseLogDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.ScreeningLogDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.MentalHealth;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.common.model.entity.spice.ScreeningLog;
import com.mdtlabs.coreplatform.common.util.UnitConversion;
import com.mdtlabs.coreplatform.spiceservice.bplog.service.BpLogService;
import com.mdtlabs.coreplatform.spiceservice.customizedmodules.service.CustomizedModulesService;
import com.mdtlabs.coreplatform.spiceservice.glucoseLog.service.GlucoseLogService;
import com.mdtlabs.coreplatform.spiceservice.mentalhealth.service.MentalHealthService;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;
import com.mdtlabs.coreplatform.spiceservice.screeningLog.repository.ScreeningLogRepository;
import com.mdtlabs.coreplatform.spiceservice.screeningLog.service.ScreeningLogService;

/**
 * This class implements the ScreeningLogService interface and contains actual
 * business logic to perform operations on screeninglog entity.
 *
 * @author Karthick Murugesan
 */
@Service
public class ScreeningLogServiceImpl implements ScreeningLogService {

	@Autowired
	private ScreeningLogRepository screeningLogRepository;

	@Autowired
	private GlucoseLogService glucoseLogService;

	@Autowired
	private BpLogService bpLogService;

	@Autowired
	private CustomizedModulesService customizedModulesService;

	@Autowired
	private PatientTrackerService patientTrackerService;

	@Autowired
	private MentalHealthService mentalHealthService;

	private ModelMapper modelMapper = new ModelMapper();

	/**
	 * {@inheritDoc}
	 */
	public ScreeningLog addScreeningLog(ScreeningLogDTO screeningLogDTO) {
		if (Objects.isNull(screeningLogDTO)) {
			throw new BadRequestException(1000);
		} else {
			if (UnitConstants.IMPERIAL == screeningLogDTO.getUnitMeasurement()) {
				screeningLogDTO.getBioMetrics().setHeight(UnitConversion
						.convertHeight(screeningLogDTO.getBioMetrics().getHeight(), UnitConstants.METRIC));
				screeningLogDTO.getBioMetrics().setWeight(UnitConversion
						.convertHeight(screeningLogDTO.getBioMetrics().getWeight(), UnitConstants.METRIC));
				// screeningLogDTO.getBioMetrics().setTempe(UnitConversion.convertHeight(screeningLogDTO.getBioMetrics(),
				// UnitConstants.METRIC));

			}
			validateScreeninigLog(screeningLogDTO);
			ScreeningLog screeningLogData = constructScreeningLogData(screeningLogDTO);
			// data.country = data.site.country.toString();
			// data.county = data.site.county.toString();
			// data.sub_county = data.site.sub_county.toString();
			// data.tenant_id = data.site.tenant_id;
			// data.operating_unit = data.site.operating_unit.toString();
			// data.account = data.site.account.toString();
			// data.site = data.site._id.toString();
			screeningLogData.setIsLatest(true);

			PatientTracker patientTracker = createPatientTracker(screeningLogDTO);
			ScreeningLog screeningLogResponse = screeningLogRepository.save(screeningLogData);
			patientTracker.setScreeningLogId(screeningLogResponse.getId());

			if (!Objects.isNull(patientTracker) && Objects.isNull(patientTracker.getId())
					&& !patientTracker.getPatientStatus().equals(Constants.ENROLLED)) {
				patientTracker = patientTrackerService.addOrUpdatePatientTracker(patientTracker);
			}

			BpLog bpLog = createBpLog(screeningLogDTO, patientTracker);
			bpLog.setScreeningId(screeningLogResponse.getId());
			BpLog bpLogResponse = bpLogService.addBpLog(bpLog, Constants.BOOLEAN_FALSE);

			if (!Objects.isNull(screeningLogDTO.getGlucoseLog())) {
				GlucoseLog glucoseLog = createGlucoseLog(screeningLogDTO, patientTracker);
				glucoseLog.setScreeningId(screeningLogResponse.getId());
				GlucoseLog glucoseLogResponse = glucoseLogService.addGlucoseLog(glucoseLog, Constants.BOOLEAN_FALSE);
			}

			if (!Objects.isNull(screeningLogDTO.getCustomizedWorkflows())
					&& !screeningLogDTO.getCustomizedWorkflows().isEmpty()) {
				customizedModulesService.createCustomizedModules(screeningLogDTO.getCustomizedWorkflows(),
						Constants.WORKFLOW_SCREENING, patientTracker.getId());
			}

			return screeningLogResponse;
		}
	}

	private ScreeningLog constructScreeningLogData(ScreeningLogDTO screeningLogDTO) {
		ScreeningLog screeningLog = new ScreeningLog();

		if (!Objects.isNull(screeningLogDTO.getBioMetrics())) {
			screeningLog.setGender(screeningLogDTO.getBioMetrics().getGender());
			screeningLog.setWeight(screeningLogDTO.getBioMetrics().getWeight());
			screeningLog.setPhysicallyActive(screeningLogDTO.getBioMetrics().getIsphysicallyActive());
			screeningLog.setAge(screeningLogDTO.getBioMetrics().getAge());
			screeningLog.setHeight(screeningLogDTO.getBioMetrics().getHeight());
			screeningLog.setRegularSmoker(screeningLogDTO.getBioMetrics().isRegularSmoker());
			screeningLog.setFamilyDiabetesHistory(screeningLogDTO.getBioMetrics().isFamilyDiabetesHistory());
			screeningLog.setBmi(screeningLogDTO.getBioMetrics().getBmi());
			screeningLog.setBeforeGestationalDiabetes(screeningLogDTO.getBioMetrics().isBeforeGestationalDiabetes());
		}

		if (!Objects.isNull(screeningLogDTO.getBioData())) {
			screeningLog.setNationalId(screeningLogDTO.getBioData().getNationalId());
			screeningLog.setPhoneNumber(screeningLogDTO.getBioData().getPhoneNumber());
			screeningLog.setLastName(screeningLogDTO.getBioData().getLastName());
			screeningLog.setPhoneNumberCategory(screeningLogDTO.getBioData().getPhoneNumberCategory());
			screeningLog.setMiddleName(screeningLogDTO.getBioData().getMiddleName());
			screeningLog.setIdType(screeningLogDTO.getBioData().getIdType());
			screeningLog.setMiddleName(screeningLogDTO.getBioData().getMiddleName());
			screeningLog.setFirstName(screeningLogDTO.getBioData().getFirstName());
			screeningLog.setLandmark(screeningLogDTO.getBioData().getLandmark());
			screeningLog.setPreferredName(screeningLogDTO.getBioData().getPreferredName());
		}

		if (!Objects.isNull(screeningLogDTO.getBpLog())) {
			// screeningLog.setIsBeforeHtnDiagnosis(screeningLogDTO.getBpLog().isBeforeHtnDiagnosis());
			screeningLog.setAvgDiastolic(screeningLogDTO.getBpLog().getAvgDiastolic());
			screeningLog.setAvgSystolic(screeningLogDTO.getBpLog().getAvgSystolic());
			screeningLog.setAvgPulse(screeningLogDTO.getBpLog().getAvgPulse());
			screeningLog.setBpArm(screeningLogDTO.getBpLog().getBpArm());
			screeningLog.setBpPosition(screeningLogDTO.getBpLog().getBpPosition());
			// screeningLog.setCovidVaccStatus(screeningLogDTO.getBpLog().getCovidVaccStatus());
			screeningLog.setBpLogDetails(screeningLogDTO.getBpLog().getBpLogDetails());
		}

		if (!Objects.isNull(screeningLogDTO.getGlucoseLog())) {
			screeningLog.setGlucoseDateTime(screeningLogDTO.getGlucoseLog().getGlucoseDateTime());
			screeningLog.setLastMealTime(screeningLogDTO.getGlucoseLog().getLastMealTime());
			screeningLog.setGlucoseType(screeningLogDTO.getGlucoseLog().getGlucoseType());
			screeningLog.setGlucoseUnit(screeningLogDTO.getGlucoseLog().getGlucoseUnit());
			screeningLog.setGlucoseValue(screeningLogDTO.getGlucoseLog().getGlucoseValue());
			// hb1ac
		}
		screeningLog.setIsReferAssessment(screeningLogDTO.getIsReferAssessment());
		screeningLog.setType(screeningLogDTO.getType());
		screeningLog.setCvdRiskScore(screeningLogDTO.getCvdRiskScore());
		screeningLog.setCvdRiskLevel(screeningLogDTO.getCvdRiskLevel());
		screeningLog.setDateOfBirth(screeningLogDTO.getDateOfBirth());
		screeningLog.setLatitude(screeningLogDTO.getLatitude());
		screeningLog.setLongitude(screeningLogDTO.getLongitude());
		// screeningLog.setCvdRiskScoreDisplay(screeningLogDTO.getCvdRiskScoreDisplay());
		screeningLog.setDeviceInfoId(screeningLogDTO.getDeviceInfoId());
		screeningLog.setCategory(screeningLogDTO.getCategory());
		// screeningLog.setUpdatedFromEnrollment(screeningLogDTO.isUpdatedFromEnrollment());
		if (!Objects.isNull(screeningLogDTO.getPhq4())) {
			screeningLog.setPhq4score(screeningLogDTO.getPhq4().getPhq4Score());
			screeningLog.setPhq4RiskLevel(screeningLogDTO.getPhq4().getPhq4RiskLevel());
			screeningLog.setPhq4MentalHealth(screeningLogDTO.getPhq4().getPhq4MentalHealth());
		}
		return screeningLog;
	}

	/**
	 * {@inheritDoc}
	 */
	public BpLog createBpLog(ScreeningLogDTO screeningLogDTO, PatientTracker patientTracker) {
		BpLog bpLog = screeningLogDTO.getBpLog();
		bpLog.setRegularSmoker(screeningLogDTO.getBioMetrics().isRegularSmoker());
		bpLog.setHeight(screeningLogDTO.getBioMetrics().getHeight());
		bpLog.setWeight(screeningLogDTO.getBioMetrics().getWeight());
		bpLog.setBmi(screeningLogDTO.getBioMetrics().getBmi());
		bpLog.setCvdRiskScore(screeningLogDTO.getCvdRiskScore());
		bpLog.setCvdRiskLevel(screeningLogDTO.getCvdRiskLevel());
		bpLog.setType(Constants.SCREENING);
		bpLog.setPatientTrackId(patientTracker.getId());
		return bpLog;
	}

	/**
	 * {@inheritDoc}
	 */
	public GlucoseLog createGlucoseLog(ScreeningLogDTO screeningLogDTO, PatientTracker patientTracker) {
		GlucoseLog glucoseLog = screeningLogDTO.getGlucoseLog();
		glucoseLog.setPatientTrackId(patientTracker.getId());
		glucoseLog.setType(Constants.SCREENING);
		return glucoseLog;
	}

	/**
	 * {@inheritDoc}
	 */
	public void validateScreeninigLog(ScreeningLogDTO screeningLog) {
		System.out.println(screeningLog.getBpLog().getBpLogDetails());
		System.out.println(screeningLog.getBpLog().getBpLogDetails().size());
		System.out.println(Objects.isNull(screeningLog.getBpLog().getBpLogDetails())
				|| 2 > screeningLog.getBpLog().getBpLogDetails().size());
		if (Objects.isNull(screeningLog.getBpLog().getBpLogDetails())
				|| 2 > screeningLog.getBpLog().getBpLogDetails().size()) {
			throw new BadRequestException(8001);
		}
		// TODO: device id verification
		// TODO: validate site
		// TODO: Update site details to data object for using in screening creation

	}

	/**
	 * {@inheritDoc}
	 */
	public PatientTracker createPatientTracker(ScreeningLogDTO screeningLog) {
		PatientTracker patientTracker = null;

		String searchNationalId = screeningLog.getBioData().getNationalId().replaceAll("[^a-zA-Z0-9]*", "");
		PatientTracker existingPatientTracker = patientTrackerService.getPatientTrackerByNationalId(searchNationalId);

		System.out.println(existingPatientTracker);
		if (!Objects.isNull(existingPatientTracker)) {
			if (existingPatientTracker.getPatientStatus().equals(Constants.ENROLLED)) {
				// TODO: screening user already exist update teneat id only
				// existingPatientTracker.setTenantId();
				updateBpAndGlucoseValues(existingPatientTracker, screeningLog);
				patientTracker = existingPatientTracker;
			} else {
				patientTracker = constructPatientTracker(existingPatientTracker, screeningLog);
			}
			if (!Objects.isNull(existingPatientTracker.getScreeningLogId())) {
				screeningLogRepository.updateLatestStatus(existingPatientTracker.getScreeningLogId(), false);
			}
		} else {
			patientTracker = constructPatientTracker(new PatientTracker(), screeningLog);
		}
		return patientTracker;
	}

	public PatientTracker constructPatientTracker(PatientTracker patientTracker, ScreeningLogDTO screeningLog) {
		patientTracker.setNationalId(screeningLog.getBioData().getNationalId());
		patientTracker.setFirstName(screeningLog.getBioData().getFirstName());
		patientTracker.setLastName(screeningLog.getBioData().getLastName());
		patientTracker.setAge(screeningLog.getBioMetrics().getAge());
		patientTracker.setDateOfBirth(screeningLog.getDateOfBirth());
		patientTracker.setGender(screeningLog.getBioMetrics().getGender());
		patientTracker.setPhoneNumber(screeningLog.getBioData().getPhoneNumber());
		patientTracker.setRegularSmoker(screeningLog.getBioMetrics().isRegularSmoker());
		patientTracker.setPatientStatus(Constants.SCREENED);
		// patientTracker.setSiteId(screeningLog.getSite());
		patientTracker.setHeight(screeningLog.getBioMetrics().getHeight());
		patientTracker.setWeight(screeningLog.getBioMetrics().getWeight());
		patientTracker.setBmi(screeningLog.getBioMetrics().getBmi());
		updateBpAndGlucoseValues(patientTracker, screeningLog);

		// patientTracker.setCountryId(screeningLog.getCountryId());
		// patientTracker.setCountryId(screeningLog.getCountryId());
		patientTracker.setScreeningReferral(screeningLog.getIsReferAssessment());
		// patientTracker.setTenantId(screeningLog.getTenantId());
		if (!Objects.isNull(screeningLog.getPhq4())) {
			MentalHealth phq4 = screeningLog.getPhq4();
			mentalHealthService.setPHQ4Score(phq4);
			patientTracker.setPhq4FirstScore(phq4.getPhq4FirstScore());
			patientTracker.setPhq4SecondScore(phq4.getPhq4SecondScore());
			// patientTrackerService.setPHQ4Score(patientTracker, screeningLog.getPhq4());
			patientTracker.setPhq4RiskLevel(screeningLog.getPhq4().getPhq4RiskLevel());
			patientTracker.setPhq4Score(screeningLog.getPhq4().getPhq4Score());
		}
		return patientTracker;
	}

	public void updateBpAndGlucoseValues(PatientTracker patientTracker, ScreeningLogDTO screeningLog) {
		patientTracker.setAvgDiastolic(screeningLog.getBpLog().getAvgDiastolic());
		patientTracker.setAvgSystolic(screeningLog.getBpLog().getAvgSystolic());
		patientTracker.setAvgPulse(screeningLog.getBpLog().getAvgPulse());

		patientTracker.setCvdRiskScore(screeningLog.getCvdRiskScore());
		patientTracker.setCvdRiskLevel(screeningLog.getCvdRiskLevel());
		if (!Objects.isNull(screeningLog.getGlucoseLog())) {
			patientTracker.setGlucoseValue(screeningLog.getGlucoseLog().getGlucoseValue());
			patientTracker.setGlucoseUnit(screeningLog.getGlucoseLog().getGlucoseUnit());
			patientTracker.setGlucoseType(screeningLog.getGlucoseLog().getGlucoseType());
		}
	}

	public ScreeningLog getByPatientTrackIdAndIsLatest(long id, boolean isLatest) {
		return screeningLogRepository.findByIdAndIsDeletedAndIsLatest(id, Constants.BOOLEAN_FALSE, isLatest);
	}

	public ScreeningLog getByIdAndIsLatest(long patientTrackId) {
		return screeningLogRepository.findByIdAndIsDeletedFalseAndIsLatestTrue(patientTrackId);
	}

	public ScreeningLog getScreeningDetails(RequestDTO screeningRequestDto) {
		// ScreeningResponseDTO screeningResponse = new ScreeningResponseDTO();
		if (Objects.isNull(screeningRequestDto.getPatientTrackId())) {
			throw new SpiceValidation();
		}
		if (Objects.isNull(screeningRequestDto.getScreeningId())) {
			throw new DataNotAcceptableException(9005);
		}
		ScreeningLog screeningLog = screeningLogRepository
				.findByIdAndIsDeletedAndIsLatest(screeningRequestDto.getScreeningId(), false, true);
		// ScreeningResponseDTO screeningResponse = modelMapper.map(screeningLog, new
		// TypeToken<ScreeningResponseDTO>() {
		// }.getType());
		GlucoseLog latestGlucoseLog = glucoseLogService
				.getGlucoseLogByPatientTrackId(screeningRequestDto.getPatientTrackId());
		BpLog latestBpLog = bpLogService.getBpLogByPatientTrackId(screeningRequestDto.getPatientTrackId());
		if (!Objects.isNull(latestBpLog)) {
			screeningLog.setHeight(latestBpLog.getHeight());
			screeningLog.setRegularSmoker(latestBpLog.isRegularSmoker());
			screeningLog.setWeight(latestBpLog.getWeight());
			screeningLog.setBpLogDetails(latestBpLog.getBpLogDetails());
			screeningLog.setBpLogId(latestBpLog.getId());
		}
		if (!Objects.isNull(latestGlucoseLog)) {
			screeningLog.setGlucoseLog(modelMapper.map(latestGlucoseLog, new TypeToken<GlucoseLogDTO>() {
			}.getType()));
			screeningLog.getGlucoseLog().setGlucoseId(latestGlucoseLog.getId());
		}

		return screeningLog;
	}

}