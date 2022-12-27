package com.mdtlabs.coreplatform.spiceservice.glucoseLog.service.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientGlucoseLogDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientSymptom;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTreatmentPlan;
import com.mdtlabs.coreplatform.common.util.Pagination;
import com.mdtlabs.coreplatform.spiceservice.glucoseLog.repository.GlucoseLogRepository;
import com.mdtlabs.coreplatform.spiceservice.glucoseLog.service.GlucoseLogService;
import com.mdtlabs.coreplatform.spiceservice.patientSymptom.service.PatientSymptomService;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;
import com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.service.PatientTreatmentPlanService;

/**
 * This class implements the GlucoseLogService interface and contains actual
 * business logic to perform operations on GlucoseLog entity.
 * 
 * @author Rajkumar
 *
 */
@Service
public class GlucoseLogServiceImpl implements GlucoseLogService {

	@Autowired
	private GlucoseLogRepository glucoseLogRepository;

	@Autowired
	private PatientTrackerService patientTrackerService;

	@Autowired
	private PatientSymptomService patientSymptomService;

	@Autowired
	private PatientTreatmentPlanService patientTreatmentPlanService;

	/**
	 * {@inheritDoc}
	 */
	public GlucoseLog addGlucoseLog(GlucoseLog glucoseLog, boolean isPatientTrackerUpdate) {

		if (Objects.isNull(glucoseLog)) {
			throw new BadRequestException(1000);
		} else {
			if (!Objects.isNull(glucoseLog.getGlucoseValue()) || !Objects.isNull(glucoseLog.getGlucoseType())
					|| !Objects.isNull(glucoseLog.getGlucoseDateTime())
					|| !Objects.isNull(glucoseLog.getLastMealTime())) {
				validateGlucoseLog(glucoseLog);
			}
			if (Objects.isNull(glucoseLog.getId())) {
				updateGlucoseLogLatestStatus(glucoseLog);
			}

			// Need to set assessment tenant id from user tenantid

			glucoseLog.setLatest(Constants.BOOLEAN_TRUE);
			if (Objects.isNull(glucoseLog.getBgTakenOn())) {
				glucoseLog.setBgTakenOn(new Date());
			}
			GlucoseLog glucoseLogResponse = glucoseLogRepository.save(glucoseLog);

			if (isPatientTrackerUpdate) {
				PatientTreatmentPlan patientTreatmentPlan = patientTreatmentPlanService
						.getPatientTreatmentPlan(glucoseLog.getPatientTrackId());
				Date nextBGAssessmentDate = null;
				if (!Objects.isNull(patientTreatmentPlan)) {
					nextBGAssessmentDate = patientTreatmentPlanService.getTreatmentPlanFollowupDate(
							patientTreatmentPlan.getBgCheckFrequency(), Constants.DEFAULT);
					// Note: There is no next bg assessment date for frequency name - pysician
					// approval pending status
				}
				patientTrackerService.UpdatePatientTrackerForGlucoseLog(glucoseLog.getPatientTrackId(), glucoseLog,
						nextBGAssessmentDate);
			}
			return glucoseLogResponse;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void validateGlucoseLog(GlucoseLog glucoseLog) {
		if (Objects.isNull(glucoseLog.getGlucoseValue()) || Objects.isNull(glucoseLog.getGlucoseType())
				|| Objects.isNull(glucoseLog.getGlucoseDateTime()) || Objects.isNull(glucoseLog.getGlucoseUnit())) {
			throw new BadRequestException(7005);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateGlucoseLogLatestStatus(GlucoseLog glucoseLog) {
		glucoseLogRepository.updateGlucoseLogLatestStatus(glucoseLog.getPatientTrackId(), false);
	}

	/**
	 * {@inheritDoc}
	 */
	public GlucoseLog getGlucoseLogById(long glucoseLogId) {
		GlucoseLog glucoseLog = glucoseLogRepository.findById(glucoseLogId).get();
		if (Objects.isNull(glucoseLog)) {
			throw new DataNotFoundException(7004);
		}
		return glucoseLog;
	}

	/**
	 * {@inheritDoc}
	 */
	public GlucoseLog getGlucoseLogByPatientTrackId(long patientTrackId) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date yesterday = cal.getTime();
		GlucoseLog glucoseLog = glucoseLogRepository.findByPatientTrackIdAndIsCreatedToday(patientTrackId, yesterday);
		if (null == glucoseLog) {
			return null;
		}
		return glucoseLog;
	}

	public GlucoseLog getGlucoseLogByPatientTrackIdAndIsLatest(long patientTrackId, boolean isLatest) {
		return glucoseLogRepository.findByPatientTrackIdAndIsDeletedAndIsLatest(patientTrackId, Constants.BOOLEAN_FALSE,
				isLatest);
	}

	public PatientGlucoseLogDTO getPatientGlucoseLogsWithSymptoms(RequestDTO requestData) {
		PatientGlucoseLogDTO glucoseLogsDTO = new PatientGlucoseLogDTO();
		if (Objects.isNull(requestData.getPatientTrackId())) {
			throw new DataNotAcceptableException(10010);
		}
		Pageable pageable = Pagination.setPagination(requestData.getPageNumber(), requestData.getLimit());
		Page<GlucoseLog> glucoseLogs = glucoseLogRepository.getGlucoseLogs(requestData.getPatientTrackId(), pageable);

		if (!glucoseLogs.isEmpty() && requestData.isLatestRequired()) {
			glucoseLogsDTO.setLatestGlucoseLog(glucoseLogs.toList().get(0));
			List<PatientSymptom> patientSymptoms = patientSymptomService
					.getSymptomsByPatientTracker(requestData.getPatientTrackId());
			List<String> names = patientSymptoms.stream()
					.map(symptom -> !Objects.isNull(symptom.getOtherSymptom()) ? symptom.getOtherSymptom()
							: symptom.getName())
					.collect(Collectors.toList());
			glucoseLogsDTO.setSymptomList(names);
		}
		if (!Objects.isNull(requestData.getSort())) {
			if (requestData.getSort().keySet().contains(FieldConstants.CREATED_AT)) {
				// Sort by patient bplog data to ascending order
				List<GlucoseLog> glucoseLogList = glucoseLogs.stream().sorted(
						(glucoselog1, glucoselog2) -> glucoselog1.getCreatedAt().compareTo(glucoselog2.getCreatedAt()))
						.collect(Collectors.toList());
				glucoseLogsDTO.setGlucoseLogList(glucoseLogList);
			}
		} else {
			System.out.println("in else block");
			glucoseLogsDTO.setGlucoseLogList(glucoseLogs.toList());
		}

		glucoseLogsDTO.setGlucoseThreshold(Map.of(Constants.FBS, Constants.FBS_MMOL_L, Constants.RBS,
				Constants.RBS_MMOL_L, Constants.UNIT, Constants.GLUCOSE_UNIT_MMOL_L));
		return glucoseLogsDTO;
	}

}
