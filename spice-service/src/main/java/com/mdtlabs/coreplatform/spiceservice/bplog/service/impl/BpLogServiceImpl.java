package com.mdtlabs.coreplatform.spiceservice.bplog.service.impl;

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
import com.mdtlabs.coreplatform.common.UnitConstants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientBpLogsDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientSymptom;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTreatmentPlan;
import com.mdtlabs.coreplatform.common.util.Pagination;
import com.mdtlabs.coreplatform.common.util.UnitConversion;
import com.mdtlabs.coreplatform.spiceservice.bplog.repository.BpLogRepository;
import com.mdtlabs.coreplatform.spiceservice.bplog.service.BpLogService;
import com.mdtlabs.coreplatform.spiceservice.patientSymptom.service.PatientSymptomService;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;
import com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.service.PatientTreatmentPlanService;

/**
 * This class implements the BpLogService interface and contains actual business
 * logic to perform operations on BpLog entity.
 * 
 * @author Karthick Murugesan
 *
 */
@Service
public class BpLogServiceImpl implements BpLogService {

	@Autowired
	private BpLogRepository bpLogRepository;

	@Autowired
	private PatientSymptomService patientSymptomService;

	@Autowired
	private PatientTrackerService patientTrackerService;

	@Autowired
	private PatientTreatmentPlanService patientTreatmentPlanService;

	/**
	 * {@inheritDoc}
	 */
	public BpLog addBpLog(BpLog bpLog, boolean isPatientTrackerUpdate) {
		if (Objects.isNull(bpLog)) {
			throw new BadRequestException(1000);
		} else {
			if (Objects.isNull(bpLog.getId())) {
				updateBpLogLatestStatus(bpLog);
			}
			bpLog.setLatest(true);

			if (bpLog.getUnitMeasurement().equals(UnitConstants.IMPERIAL)) {
				bpLog = convertBpLogUnits(bpLog, UnitConstants.METRIC);
			}
			if (Objects.isNull(bpLog.getBpTakenOn())) {
				bpLog.setBpTakenOn(new Date());
			}
			// User tenantid set as a assessment tenant id
			BpLog bpLogResponce = bpLogRepository.save(bpLog);
			if (isPatientTrackerUpdate) {
				PatientTreatmentPlan patientTreatmentPlan = patientTreatmentPlanService
						.getPatientTreatmentPlan(bpLog.getPatientTrackId());
				Date nextBpAssessmentDate = null;
				if (!Objects.isNull(patientTreatmentPlan)) {
					nextBpAssessmentDate = patientTreatmentPlanService.getTreatmentPlanFollowupDate(
							patientTreatmentPlan.getBpCheckFrequency(), Constants.DEFAULT);
				}
				patientTrackerService.UpdatePatientTrackerForBpLog(bpLog.getPatientTrackId(), bpLog,
						nextBpAssessmentDate);
			}
			return bpLogResponce;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public BpLog convertBpLogUnits(BpLog bpLog, String unit) {
		if (!Objects.isNull(bpLog.getHeight())) {
			bpLog.setHeight(UnitConversion.convertHeight(bpLog.getHeight(), unit));
		}
		if (!Objects.isNull(bpLog.getWeight())) {
			bpLog.setWeight(UnitConversion.convertWeight(bpLog.getWeight(), unit));
		}
		if (!Objects.isNull(bpLog.getTemperature())) {
			bpLog.setTemperature(UnitConversion.convertTemperature(bpLog.getTemperature(), unit));
		}
		return bpLog;
	}

	/**
	 * {@inheritDoc}
	 */
	public void updateBpLogLatestStatus(BpLog bpLog) {
		bpLogRepository.updateBpLogLatestStatus(bpLog.getPatientTrackId(), false);
	}

	/**
	 * {@inheritDoc}
	 */
	public BpLog getBpLogByPatientTrackId(long patientTrackId) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		Date yesterday = cal.getTime();
		BpLog bpLog = bpLogRepository.findByPatientTrackIdAndIsCreatedToday(patientTrackId, yesterday);
		return bpLog;
	}

	public BpLog getBpLogByPatientTrackIdAndIsLatest(long patientTrackId, boolean isLatest) {
		return bpLogRepository.findBypatientTrackIdAndIsDeletedAndIsLatest(patientTrackId, Constants.BOOLEAN_FALSE,
				isLatest);

	}

	public PatientBpLogsDTO getPatientBPLogsWithSymptoms(RequestDTO requestData) {
		PatientBpLogsDTO bpLogsDTO = new PatientBpLogsDTO();
		if (Objects.isNull(requestData.getPatientTrackId())) {
			throw new DataNotFoundException(4004);
		}
		Pageable pageable = Pagination.setPagination(requestData.getPageNumber(), requestData.getLimit());
		Page<BpLog> bplogs = bpLogRepository.getBpLogs(requestData.getPatientTrackId(), pageable);

		if (!bplogs.isEmpty() && requestData.isLatestRequired()) {
			bpLogsDTO.setLatestBpLog(bplogs.toList().get(0));
			List<PatientSymptom> patientSymptoms = patientSymptomService
					.getSymptomsByPatientTracker(requestData.getPatientTrackId());
			List<String> names = patientSymptoms.stream()
					.map(symptom -> !Objects.isNull(symptom.getOtherSymptom()) ? symptom.getOtherSymptom()
							: symptom.getName())
					.collect(Collectors.toList());
			bpLogsDTO.setSymptomList(names);
		}
		if (!Objects.isNull(requestData.getSort())) {
			if (requestData.getSort().keySet().contains(FieldConstants.CREATED_AT)) {
				// Sort by patient bplog data to ascending order
				List<BpLog> bplogList = bplogs.stream()
						.sorted((bplog1, bplog2) -> bplog1.getCreatedAt().compareTo(bplog2.getCreatedAt()))
						.collect(Collectors.toList());
				bpLogsDTO.setBpLogList(bplogList);
			}
		} else {
			bpLogsDTO.setBpLogList(bplogs.toList());
		}
		// Map<String, Integer> bpThreshold = Map.of(Constants.SYSTOLIC,
		// Constants.BP_THRESHOLD_SYSTOLIC, Constants.DIASTOLIC,
		// Constants.BP_THRESHOLD_DIASTOLIC);
		bpLogsDTO.setBpThreshold(Map.of(Constants.SYSTOLIC, Constants.BP_THRESHOLD_SYSTOLIC, Constants.DIASTOLIC,
				Constants.BP_THRESHOLD_DIASTOLIC));
		return bpLogsDTO;
	}

}
