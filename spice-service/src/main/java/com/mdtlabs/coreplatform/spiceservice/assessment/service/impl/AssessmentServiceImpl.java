package com.mdtlabs.coreplatform.spiceservice.assessment.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.Condition;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.contexts.UserSelectedTenantContextHolder;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.model.dto.spice.AssessmentDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.AssessmentResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.BpLogDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.ComplianceDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.GlucoseLogDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.MentalHealthDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientDetailDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RiskAlgorithmDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SymptomDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.AssessmentLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.MentalHealth;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientMedicalCompliance;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientSymptom;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTreatmentPlan;
import com.mdtlabs.coreplatform.common.model.entity.spice.RedRiskNotification;
import com.mdtlabs.coreplatform.spiceservice.NotificationApiInterface;
import com.mdtlabs.coreplatform.spiceservice.assessment.repository.AssessmentLogRepository;
import com.mdtlabs.coreplatform.spiceservice.assessment.service.AssessmentService;
import com.mdtlabs.coreplatform.spiceservice.bplog.repository.BpLogRepository;
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
	private AssessmentLogRepository assessmentLogRepository;

	@Autowired
	private BpLogRepository bpLogRepository;

	@Autowired
	private NotificationApiInterface apiInterface;

	private ModelMapper mapper = new ModelMapper();

	/**
	 * {@inheritDoc}
	 */
	public AssessmentResponseDTO createAssessment(AssessmentDTO assessmentDTO) {
		AssessmentResponseDTO response = new AssessmentResponseDTO();
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

			PatientDetailDTO patientDetails = new PatientDetailDTO();
			patientDetails.setFirstName(patientTracker.getFirstName());
			patientDetails.setId(patientTracker.getId());
			patientDetails.setGender(patientTracker.getGender());
			patientDetails.setAge(patientTracker.getAge());
			patientDetails.setLastName(patientTracker.getLastName());
			patientDetails.setNationalId(patientTracker.getNationalId());
			patientDetails.setPatientStatus(patientTracker.getPatientStatus());
			patientDetails.setProgramId(patientTracker.getProgramId());
			patientDetails.setSiteName("siteName");
			response.setPatientDetails(patientDetails);

			BpLog bpLog = constructBpLog(assessmentDTO, riskLevel);
			bpLog = bplLogService.addBpLog(bpLog, Constants.BOOLEAN_FALSE);
			response.setBpLog(new BpLogDTO(bpLog.getAvgSystolic(), bpLog.getAvgDiastolic(), bpLog.getBmi(), bpLog.getCvdRiskLevel(), bpLog.getCvdRiskScore()));
			Long glucoseId = null;
			if (!Objects.isNull(assessmentDTO.getGlucoseLog())
					&& (!Objects.isNull(assessmentDTO.getGlucoseLog().getGlucoseValue())
							|| !Objects.isNull(assessmentDTO.getGlucoseLog().getHb1ac()))) {
				GlucoseLog glucoseLog = constructGlucoseLog(assessmentDTO);
				glucoseLog = glucoseLogService.addGlucoseLog(glucoseLog, Constants.BOOLEAN_FALSE);
				response.setGlucoseLog(new GlucoseLogDTO(
					glucoseLog.getGlucoseType(),
					glucoseLog.getGlucoseValue(),
					glucoseLog.getGlucoseUnit()));
				glucoseId = glucoseLog.getId();
			}
			Long assessmentLogId = assessmentLogRepository.save(new AssessmentLog(bpLog.getId(), glucoseId)).getId();
			updatePatientTracker(patientTracker, assessmentDTO);
			if (riskLevel.equals(Constants.HIGH)) {
				patientTracker.setRedRiskPatient(Constants.BOOLEAN_TRUE);
				createRedRiskNotification(patientTracker, bpLog.getId(), glucoseId, assessmentLogId);

			}

			if (!Objects.isNull(assessmentDTO.getSymptoms()) && !assessmentDTO.getSymptoms().isEmpty()) {
				List<PatientSymptom> patientSymptoms = createPatientSymptoms(assessmentDTO.getSymptoms(), bpLog.getId(), glucoseId,
						assessmentDTO.getPatientTrackId(), assessmentLogId);
				response.setSymptoms(patientSymptoms);
				
			}
			if (!Objects.isNull(assessmentDTO.getCompliances()) && !assessmentDTO.getCompliances().isEmpty()) {
				List<PatientMedicalCompliance> patientMedicalCompliances =  createPatientCompliances(assessmentDTO.getCompliances(), bpLog.getId(), assessmentDTO.getPatientTrackId(), assessmentLogId);
				response.setMedicalCompliance(patientMedicalCompliances);
			}
			if (!Objects.isNull(assessmentDTO.getPhq4())) {
				MentalHealth mentalHealth = mapper.map(assessmentDTO.getPhq4(),
				new TypeToken<MentalHealth>() {
				}.getType());
				mentalHealthService.setPHQ4Score(mentalHealth);
				mentalHealth.setPatientTrackId(assessmentDTO.getPatientTrackId());
				patientTracker.setPhq4RiskLevel(mentalHealth.getPhq4RiskLevel());
				patientTracker.setPhq4Score(mentalHealth.getPhq4Score());
				patientTracker.setPhq4FirstScore(mentalHealth.getPhq4FirstScore());
				patientTracker.setPhq4SecondScore(mentalHealth.getPhq4SecondScore());
				mentalHealth = mentalHealthService.createMentalHealth(mentalHealth);
				response.setPhq4(new MentalHealthDTO(mentalHealth.getPhq4RiskLevel(), mentalHealth.getPhq4Score()));
			}
			if (patientTracker.getPatientStatus().equals(Constants.SCREENED)) {
				patientTracker.setPatientStatus(Constants.NONE);
			}
			patientTracker.setLastAssessmentDate(new Date());
			if (!Objects.isNull(assessmentDTO.getProvisionalDiagnosis())
					&& !assessmentDTO.getProvisionalDiagnosis().isEmpty()) {
				patientTracker.setProvisionalDiagnosis(assessmentDTO.getProvisionalDiagnosis());
			}
			if (patientTracker.getPatientStatus().equals(Constants.ENROLLED)) {
				// patientTracker.setTenantId(UserSelectedTenantContextHolder.get());
			}
			PatientTreatmentPlan patientTreatmentPlan = patientTreatmentPlanService
					.getPatientTreatmentPlan(patientTracker.getId());
			if (!Objects.isNull(patientTreatmentPlan)) {
				updateTreatmentPlan(patientTracker, assessmentDTO, patientTreatmentPlan);
			}
			if (!Objects.isNull(assessmentDTO.getCustomizedWorkflows())
                    && !assessmentDTO.getCustomizedWorkflows().isEmpty()) {
                customizedModulesService.createCustomizedModules(assessmentDTO.getCustomizedWorkflows(),
                        Constants.WORKFLOW_ASSESSMENT, patientTracker.getId());
            }
			patientTrackerService.addOrUpdatePatientTracker(patientTracker);
		}
		return response;
	}

	// private 


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
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
		BpLog bpLog = mapper.map(assessmentDTO.getBpLog(),
                new TypeToken<BpLog>() {
                }.getType());
		bpLog.setCvdRiskScore(assessmentDTO.getCvdRiskScore());
		bpLog.setCvdRiskLevel(assessmentDTO.getCvdRiskLevel());
		bpLog.setRegularSmoker(assessmentDTO.getIsRegularSmoker());
		bpLog.setType(Constants.ASSESSMENT);
		bpLog.setPatientTrackId(assessmentDTO.getPatientTrackId());
		bpLog.setRegularSmoker(assessmentDTO.getIsRegularSmoker());
		bpLog.setTenantId(assessmentDTO.getTenantId());
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
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
		GlucoseLog glucoseLog = mapper.map(assessmentDTO.getGlucoseLog(),
				new TypeToken<GlucoseLog>() {
				}.getType());
		glucoseLog.setPatientTrackId(assessmentDTO.getPatientTrackId());
		glucoseLog.setType(Constants.ASSESSMENT);
		// Need to get tenantId from header
		glucoseLog.setTenantId(assessmentDTO.getTenantId());
		glucoseLog.setTenantId(assessmentDTO.getTenantId());
		// glucoseLog.setPatientTrackerId(assessmentDTO.getPatientTrackId());
		//tenantid add
		return glucoseLog;
	}

    /**
     * Creates a red risk notification for a patient
     *
     * @param patientTracker PatientTracker entity
     * @param bpLogId        bpLog id
     * @param glucoseLogId   glucoseLog id
     */
    public void createRedRiskNotification(PatientTracker patientTracker, Long bpLogId, Long glucoseLogId, Long assessmentLogId) {
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
    public List<PatientSymptom> createPatientSymptoms(Set<SymptomDTO> symptomDTOs, Long bpLogId, Long glucoseLogId,
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
        return patientSymptomService.addPatientSymptoms(patientSymptomList);
    }

    /**
     * Creates the patient compliances
     *
     * @param compliances
     * @param bpLogId
     * @param patientTrackId
     */
    public List<PatientMedicalCompliance> createPatientCompliances(Set<ComplianceDTO> compliances, Long bpLogId, Long patientTrackId, Long assessmentLogId) {
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
        return patientMedicalComplianceService.addPatientMedicalCompliance(patientMedicalCompliances);
	}


}
