package com.mdtlabs.coreplatform.spiceservice.assessment.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.model.dto.spice.AssessmentDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.ComplianceDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RiskAlgorithmDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SymptomDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.MentalHealth;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientAssessment;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientMedicalCompliance;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientSymptom;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTreatmentPlan;
import com.mdtlabs.coreplatform.common.model.entity.spice.RedRiskNotification;
import com.mdtlabs.coreplatform.spiceservice.assessment.repository.PatientAssessmentRepository;
import com.mdtlabs.coreplatform.spiceservice.assessment.service.AssessmentService;
import com.mdtlabs.coreplatform.spiceservice.bplog.service.BpLogService;
import com.mdtlabs.coreplatform.spiceservice.common.RedRiskService;
import com.mdtlabs.coreplatform.spiceservice.common.RiskAlgorithm;
import com.mdtlabs.coreplatform.spiceservice.customizedmodules.service.CustomizedModulesService;
import com.mdtlabs.coreplatform.spiceservice.glucoseLog.service.GlucoseLogService;
import com.mdtlabs.coreplatform.spiceservice.mentalhealth.service.MentalHealthService;
import com.mdtlabs.coreplatform.spiceservice.patientSymptom.service.PatientSymptomService;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;
import com.mdtlabs.coreplatform.spiceservice.patientmedicalcompliance.service.PatientMedicalComplianceService;
import com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.service.PatientTreatmentPlanService;

/**
 * This class implements the AssessmentService interface and contains actual
 * business logic to perform operations on assessment entity.
 *
 * @author Karthick Murugesan
 */
@Service
@Validated
//@ValidateOnExecution(type = ExecutableType.ALL)
public class AssessmentServiceImpl implements AssessmentService {

	@Autowired
	private BpLogService bplLogService;

	@Autowired
	private GlucoseLogService glucoseLogService;

	@Autowired
	private PatientTrackerService patientTrackerService;

	@Autowired
	private PatientSymptomService patientSymptomService;

	@Autowired
	private PatientMedicalComplianceService patientMedicalComplianceService;

	@Autowired
	private PatientTreatmentPlanService patientTreatmentPlanService;

	@Autowired
	private MentalHealthService mentalHealthService;

	@Autowired
	private CustomizedModulesService customizedModulesService;

	@Autowired
	private PatientAssessmentRepository assessmentLogRepository;

	/**
	 * {@inheritDoc}
	 */
	public AssessmentDTO createAssessment(AssessmentDTO assessmentDTO) {
		if (Objects.isNull(assessmentDTO)) {
			throw new BadRequestException(1000);
		} else {
			String riskLevel = "";
			String riskMessage;

			PatientTracker patientTracker = patientTrackerService
					.getPatientTrackerById(assessmentDTO.getPatientTrackId());
			if (patientTracker.getPatientStatus().equals(Constants.ENROLLED) && patientTracker.isInitialReview()
					&& !patientTracker.isRedRiskPatient()) {
				riskLevel = calculateRiskLevel(assessmentDTO, patientTracker);
				riskLevel = RedRiskService.convertRiskLevelToBPRiskLevel(riskLevel);
				riskMessage = RedRiskService.calculateRiskMessage(riskLevel);
			}

			BpLog bpLog = constructBpLog(assessmentDTO, riskLevel);
			bpLog = bplLogService.addBpLog(bpLog, Constants.BOOLEAN_FALSE);
			Long glucoseId = null;
			if (!Objects.isNull(assessmentDTO.getGlucoseLog())
					&& (!Objects.isNull(assessmentDTO.getGlucoseLog().getGlucoseValue())
							|| !Objects.isNull(assessmentDTO.getGlucoseLog().getHb1ac()))) {
				GlucoseLog glucoseLog = constructGlucoseLog(assessmentDTO);
				glucoseLog = glucoseLogService.addGlucoseLog(glucoseLog, Constants.BOOLEAN_FALSE);
				glucoseId = glucoseLog.getId();
			}
			Long assessmentLogId = assessmentLogRepository.save(new PatientAssessment(bpLog.getId(), glucoseId))
					.getId();
			updatePatientTracker(patientTracker, assessmentDTO);
			if (riskLevel.equals(Constants.HIGH)) {
				patientTracker.setRedRiskPatient(Constants.BOOLEAN_TRUE);
				createRedRiskNotification(patientTracker, bpLog.getId(), glucoseId, assessmentLogId);
			}

			if (!Objects.isNull(assessmentDTO.getSymptoms()) && !assessmentDTO.getSymptoms().isEmpty()) {
				createPatientSymptoms(assessmentDTO.getSymptoms(), bpLog.getId(), glucoseId,
						assessmentDTO.getPatientTrackId(), assessmentLogId);
			}
			if (!Objects.isNull(assessmentDTO.getCompliances()) && !assessmentDTO.getCompliances().isEmpty()) {
				createPatientCompliances(assessmentDTO.getCompliances(), bpLog.getId(),
						assessmentDTO.getPatientTrackId(), assessmentLogId);
			}
			if (!Objects.isNull(assessmentDTO.getPhq4())) {
				MentalHealth mentalHealth = assessmentDTO.getPhq4();
				mentalHealthService.setPHQ4Score(mentalHealth);
				mentalHealth.setPatientTrackId(assessmentDTO.getPatientTrackId());
				patientTracker.setPhq4RiskLevel(mentalHealth.getPhq4RiskLevel());
				patientTracker.setPhq4Score(mentalHealth.getPhq4Score());
				patientTracker.setPhq4FirstScore(mentalHealth.getPhq4FirstScore());
				patientTracker.setPhq4SecondScore(mentalHealth.getPhq4SecondScore());
				mentalHealthService.createMentalHealth(mentalHealth);
			}
			// TODO : Mental health create
			if (patientTracker.getPatientStatus().equals(Constants.SCREENED)) {
				patientTracker.setPatientStatus(Constants.NONE);
			}
			patientTracker.setLastAssessmentDate(new Date());
			// patientTrackerService.setPHQ4Score(patientTracker, assessmentDTO.getPhq4());
			if (!Objects.isNull(assessmentDTO.getProvisionalDiagnosis())
					&& !assessmentDTO.getProvisionalDiagnosis().isEmpty()) {
				patientTracker.setProvisionalDiagnosis(assessmentDTO.getProvisionalDiagnosis());
			}
			if (patientTracker.getPatientStatus().equals(Constants.ENROLLED)) {
				// Updating logged in user tenant_id in patient tracker -
				// ${params.user.tenant_id}`);

			}
			PatientTreatmentPlan patientTreatmentPlan = patientTreatmentPlanService
					.getPatientTreatmentPlan(patientTracker.getId());
			if (!Objects.isNull(patientTreatmentPlan)) {
				updateTreatmentPlan(patientTracker, assessmentDTO, patientTreatmentPlan);
			}

			// TODO : customized workflow
			if (!Objects.isNull(assessmentDTO.getCustomizedWorkflows())
					&& !assessmentDTO.getCustomizedWorkflows().isEmpty()) {
				customizedModulesService.createCustomizedModules(assessmentDTO.getCustomizedWorkflows(),
						Constants.WORKFLOW_ASSESSMENT, patientTracker.getId());
			}
			patientTrackerService.addOrUpdatePatientTracker(patientTracker);
		}
		return null;
	}

	private void updateTreatmentPlan(PatientTracker patientTracker, AssessmentDTO assessmentDTO,
			PatientTreatmentPlan patientTreatmentPlan) {
		Date nextBPAssessmentDate = patientTreatmentPlanService
				.getTreatmentPlanFollowupDate(patientTreatmentPlan.getBpCheckFrequency(), Constants.DEFAULT);
		patientTracker.setNextBpAssessmentDate(nextBPAssessmentDate);
		if (!Objects.isNull(assessmentDTO.getGlucoseLog())) {
			Date nextBGAssessmentDate = patientTreatmentPlanService
					.getTreatmentPlanFollowupDate(patientTreatmentPlan.getBgCheckFrequency(), Constants.DEFAULT);
			// Note: There is no next bg assessment date for frequency name - pysician
			// approval pending status
			if (!Objects.isNull(nextBGAssessmentDate)) {
				patientTracker.setNextBgAssessmentDate(nextBGAssessmentDate);
			}

		}

	}

	/**
	 * Updates the Bp and glucose valuse in patient tracker
	 *
	 * @param patientTracker PatientTracker entity
	 * @param assessmentDTO  AssessmentDTO Entity
	 */
	public void updatePatientTracker(PatientTracker patientTracker, AssessmentDTO assessmentDTO) {
		patientTracker.setAvgDiastolic(assessmentDTO.getBpLog().getAvgDiastolic());
		patientTracker.setAvgSystolic(assessmentDTO.getBpLog().getAvgSystolic());
		patientTracker.setAvgPulse(assessmentDTO.getBpLog().getAvgPulse());
		patientTracker.setHeight(assessmentDTO.getBpLog().getHeight());
		patientTracker.setWeight(assessmentDTO.getBpLog().getWeight());
		patientTracker.setBmi(assessmentDTO.getBpLog().getBmi());
		patientTracker.setCvdRiskScore(assessmentDTO.getCvdRiskScore());
		patientTracker.setCvdRiskLevel(assessmentDTO.getCvdRiskLevel());
		if (!Objects.isNull(assessmentDTO.getGlucoseLog())) {
			patientTracker.setGlucoseValue(assessmentDTO.getGlucoseLog().getGlucoseValue());
			patientTracker.setGlucoseUnit(assessmentDTO.getGlucoseLog().getGlucoseUnit());
			patientTracker.setGlucoseType(assessmentDTO.getGlucoseLog().getGlucoseType());
		}
	}

	/**
	 * Caluculate the risk level of the patient
	 *
	 * @param assessment     AssessmentDTO DTO
	 * @param patientTracker PatientTracker entity
	 * @return String risklevel
	 */
	public String calculateRiskLevel(AssessmentDTO assessment, PatientTracker patientTracker) {
		RiskAlgorithmDTO riskAlgorithm = new RiskAlgorithmDTO();
		riskAlgorithm.setPatientTrackId(assessment.getPatientTrackId());
		riskAlgorithm.setGlucoseType(assessment.getGlucoseLog().getGlucoseType());
		riskAlgorithm.setGlucoseValue(assessment.getGlucoseLog().getGlucoseValue());
		riskAlgorithm.setAvgDiastolic(assessment.getBpLog().getAvgDiastolic());
		riskAlgorithm.setAvgSystolic(assessment.getBpLog().getAvgSystolic());
		riskAlgorithm.setIsPregnant(patientTracker.getIsPregnant());
		riskAlgorithm.setRiskLevel(patientTracker.getRiskLevel());
		riskAlgorithm.setSymptoms(
				assessment.getSymptoms().stream().map(symptom -> symptom.getSymptomId()).collect(Collectors.toSet()));

		return RiskAlgorithm.getRiskLevelInAssessmentDBM(riskAlgorithm);
		// riskLevel = RedRiskService.convertRiskLevelToBPRiskLevel(riskLevel);
		// riskMessage = RedRiskService.calculateRiskMessage(riskLevel);
	}

	/**
	 * Constructs a BpLog from assessmentDTO
	 * 
	 * @param assessmentDTO AssessmentDTO
	 * @param riskLevel     string
	 * @return BpLog
	 */
	public BpLog constructBpLog(AssessmentDTO assessmentDTO, String riskLevel) {
		BpLog bpLog = assessmentDTO.getBpLog();
		bpLog.setCvdRiskScore(assessmentDTO.getCvdRiskScore());
		bpLog.setCvdRiskLevel(assessmentDTO.getCvdRiskLevel());
		bpLog.setType(Constants.ASSESSMENT);
		bpLog.setPatientTrackId(assessmentDTO.getPatientTrackId());
		// bpLog.setTenantId(assessmentDTO);
		bpLog.setRiskLevel(riskLevel);
		return bpLog;
	}

	/**
	 * Constructs Glucose log from assessmentDTO
	 * 
	 * @param assessmentDTO AssessmentDTO
	 * @return GlucoseLog a GlucoseLog entity
	 */
	public GlucoseLog constructGlucoseLog(AssessmentDTO assessmentDTO) {
		GlucoseLog glucoseLog = assessmentDTO.getGlucoseLog();
		glucoseLog.setPatientTrackId(assessmentDTO.getPatientTrackId());
		glucoseLog.setType(Constants.ASSESSMENT);
		// glucoseLog.setPatientTrackerId(assessmentDTO.getPatientTrackId());
		// tenantid add
		return glucoseLog;
	}

	/**
	 * Creates a red risk notification for a patient
	 *
	 * @param patientTracker PatientTracker entity
	 * @param bpLogId        bpLog id
	 * @param glucoseLogId   glucoseLog id
	 */
	public void createRedRiskNotification(PatientTracker patientTracker, Long bpLogId, Long glucoseLogId,
			Long assessmentLogId) {
		RedRiskNotification redRiskNotification = new RedRiskNotification();
		redRiskNotification.setPatientTrackId(patientTracker.getId());
		redRiskNotification.setBpLogId(bpLogId);
		redRiskNotification.setGlucoseLogId(glucoseLogId);
		redRiskNotification.setTenentId(patientTracker.getTenantId());
		redRiskNotification.setAssessmentLogId(assessmentLogId);
		redRiskNotification.setStatus(Constants.NEW);
		RedRiskService.createRedRiskNotification(redRiskNotification);

	}

	/**
	 * Create a patient symptoms
	 *
	 * @param symptomDTOs      set of SymptomDTO
	 * @param bpLogId          bpLog id
	 * @param glucoseLogId     glucoseLog id
	 * @param patientTrackerId patientTracker id
	 */
	public void createPatientSymptoms(Set<SymptomDTO> symptomDTOs, Long bpLogId, Long glucoseLogId,
			Long patientTrackerId, Long assessmentLogId) {
		List<PatientSymptom> patientSymptomList = new ArrayList<>();

		for (SymptomDTO symptom : symptomDTOs) {
			PatientSymptom patientSymptom = new PatientSymptom();
			if (Constants.HYPERTENSION.equals(symptom.getType())) {
				patientSymptom.setBpLogId(bpLogId);
			}
			if (Constants.DIABETES.equals(symptom.getType()) && !Objects.isNull(glucoseLogId)) {
				patientSymptom.setGlucoseLogId(glucoseLogId);
			}
			if (symptom.getType() == Constants.OTHER) {
				patientSymptom.setBpLogId(bpLogId);
				if (!Objects.isNull(glucoseLogId)) {
					patientSymptom.setGlucoseLogId(glucoseLogId);
				}
			}
			patientSymptom.setType(symptom.getType());
			patientSymptom.setName(symptom.getName());
			patientSymptom.setSymptomId(symptom.getSymptomId());
			patientSymptom.setPatientTrackerId(patientTrackerId);
			patientSymptom.setAssessmentLogId(assessmentLogId);
			patientSymptomList.add(patientSymptom);
		}
		patientSymptomService.addPatientSymptoms(patientSymptomList);
	}

	/**
	 * Creates the patient compliances
	 *
	 * @param compliances
	 * @param bpLogId
	 * @param patientTrackId
	 */
	public void createPatientCompliances(Set<ComplianceDTO> compliances, Long bpLogId, Long patientTrackId,
			Long assessmentLogId) {
		List<PatientMedicalCompliance> patientMedicalCompliances = new ArrayList<>();
		for (ComplianceDTO compliance : compliances) {
			PatientMedicalCompliance patientMedicalCompliance = new PatientMedicalCompliance();
			patientMedicalCompliance.setBpLogId(bpLogId);
			patientMedicalCompliance.setComplianceId(compliance.getComplianceId());
			patientMedicalCompliance.setPatientTrackId(patientTrackId);
			patientMedicalCompliance.setName(compliance.getName());
			patientMedicalCompliance.setAssessmentLogId(assessmentLogId);
			patientMedicalCompliance.setOtherCompliance(compliance.getOtherCompliance());
			patientMedicalCompliances.add(patientMedicalCompliance);
		}
		patientMedicalComplianceService.addPatientMedicalCompliance(patientMedicalCompliances);

	}
}
