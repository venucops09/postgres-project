package com.mdtlabs.coreplatform.spiceservice.medicalreview.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.SpiceValidation;
import com.mdtlabs.coreplatform.common.model.dto.spice.CurrentMedicationDetailsDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.DiagnosisDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.InitialMedicalReviewDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.MedicalReviewDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.MedicalReviewResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientMedicalReviewDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RedRiskDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientComorbidity;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientComplication;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientCurrentMedication;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientDiagnosis;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientLifestyle;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientMedicalReview;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTreatmentPlan;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientVisit;
import com.mdtlabs.coreplatform.common.model.entity.spice.Prescription;
import com.mdtlabs.coreplatform.common.model.entity.spice.PrescriptionHistory;
import com.mdtlabs.coreplatform.spiceservice.common.RedRiskService;
import com.mdtlabs.coreplatform.spiceservice.common.repository.ComplaintsRepository;
import com.mdtlabs.coreplatform.spiceservice.common.repository.PatientComorbidityRepository;
import com.mdtlabs.coreplatform.spiceservice.common.repository.PatientComplicationRepository;
import com.mdtlabs.coreplatform.spiceservice.common.repository.PatientCurrentMedicationRepository;
import com.mdtlabs.coreplatform.spiceservice.common.repository.PatientDiagnosisRepository;
import com.mdtlabs.coreplatform.spiceservice.common.repository.PatientLifestyleRepository;
import com.mdtlabs.coreplatform.spiceservice.common.repository.PhysicalExaminationRepository;
import com.mdtlabs.coreplatform.spiceservice.medicalreview.repository.MedicalReviewRepository;
import com.mdtlabs.coreplatform.spiceservice.medicalreview.service.MedicalReviewService;
import com.mdtlabs.coreplatform.spiceservice.patientNutritionLifestyle.repository.PatientNutritionLifestyleRepository;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;
import com.mdtlabs.coreplatform.spiceservice.patientlabtest.service.PatientLabTestService;
import com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.service.PatientTreatmentPlanService;
import com.mdtlabs.coreplatform.spiceservice.patientvisit.service.PatientVisitService;
import com.mdtlabs.coreplatform.spiceservice.prescription.repository.PrescriptionHistoryRepository;
import com.mdtlabs.coreplatform.spiceservice.prescription.repository.PrescriptionRepository;

/**
 * This class implements the MedicalReviewService interface and contains actual
 * business logic to perform operations on medical review entity.
 * 
 * @author Karthick Murugesan
 *
 */
@Service
public class MedicalReviewServiceImpl implements MedicalReviewService {

	@Autowired
	private PatientTrackerService patientTrackerService;

	@Autowired
	private PatientDiagnosisRepository patientDiagnosisRepository;

	@Autowired
	private PatientComorbidityRepository patientComorbidityRepository;

	@Autowired
	private PatientComplicationRepository patientComplicationRepository;

	@Autowired
	private PatientLifestyleRepository patientLifestyleRepository;

	@Autowired
	private PatientCurrentMedicationRepository patientCurrentMedicationRepository;

	@Autowired
	private PhysicalExaminationRepository physicalExaminationRepository;

	@Autowired
	private ComplaintsRepository complaintsRepository;

	@Autowired
	private MedicalReviewRepository medicalReviewRepository;

	@Autowired
	private PatientTreatmentPlanService patientTreatmentPlanService;

	@Autowired
	private PatientVisitService patientVisitService;

	@Autowired
	private PatientLabTestService patientLabTestService;

	@Autowired
	private PrescriptionRepository prescriptionRepository;

	@Autowired
	private PrescriptionHistoryRepository prescriptionHistoryRepository;

	@Autowired
	private PatientNutritionLifestyleRepository lifestyleRepository;
//	@Autowired
//	private ComorbidityService comorbidityService;
//	@Autowired
//	private ComplicationService complicationService;
//	@Autowired
//	private LifestyleService lifestyleService;
//	@Autowired
//	private PhysicalExaminationService physicalExaminationService;
//	@Autowired
//	private ComplaintsService complaintsService;
//	@Autowired
//	private CurrentMedicationService currentMedicationService;
//	@Autowired
//	private FrequencyService frequencyService;

	/**
	 * {@inheritDoc}
	 */
	public MedicalReviewDTO addMedicalReview(MedicalReviewDTO medicalReviewDTO) {

		if (Objects.isNull(medicalReviewDTO.getPatientTrackId())) {
			throw new BadRequestException(10010);
		}
		if (Objects.isNull(medicalReviewDTO.getContinuousMedicalReview())) {
			throw new BadRequestException(6001);
		}
		PatientTracker patientTracker = patientTrackerService
				.getPatientTrackerById(medicalReviewDTO.getPatientTrackId());
		System.out.println("medicalrevire ++++++++++++++" + medicalReviewDTO);
		System.out.println("initial medical review ++++++++++++" + medicalReviewDTO.getInitialMedicalReview());
		System.out.println(!Objects.isNull(medicalReviewDTO.getInitialMedicalReview()));
		if (!Objects.isNull(medicalReviewDTO.getInitialMedicalReview())) {
			createPatientInitialEncounter(medicalReviewDTO, patientTracker);
		}
		if (!Objects.isNull(medicalReviewDTO.getContinuousMedicalReview())) {
			createContinuousMedicalReview(medicalReviewDTO, patientTracker);
		}

		return null;
	}

	/**
	 * Creates continuous medical review for given patient
	 * 
	 * @param medicalReviewDTO medicalReviewDTO request date
	 * @param patientTracker   PatientTracker
	 */
	private void createContinuousMedicalReview(MedicalReviewDTO medicalReviewDTO, PatientTracker patientTracker) {
		if (!patientTracker.isInitialReview()) {
			throw new DataNotAcceptableException(6002);
		}
		PatientMedicalReview patientMedicalReview = new PatientMedicalReview();
		patientMedicalReview.setTenantId(medicalReviewDTO.getTenantId());
		patientMedicalReview.setPatientTrackId(medicalReviewDTO.getPatientTrackId());
		patientMedicalReview.setPatientVisitId(medicalReviewDTO.getPatientVisitId());
		patientMedicalReview
				.setPhysicalExamComments(medicalReviewDTO.getContinuousMedicalReview().getPhysicalExamComments());
		patientMedicalReview.setCompliantComments(medicalReviewDTO.getContinuousMedicalReview().getComplaintComments());
		patientMedicalReview.setComplaints(
				complaintsRepository.getComplaintsByIds(medicalReviewDTO.getContinuousMedicalReview().getComplaints()));
		patientMedicalReview.setPhysicalExams(physicalExaminationRepository
				.getPhysicalExaminationByIds(medicalReviewDTO.getContinuousMedicalReview().getPhysicalExams()));
		System.out.println(patientMedicalReview);
		medicalReviewRepository.save(patientMedicalReview);
		PatientVisit patientVisit = patientVisitService.getPatientVisit(medicalReviewDTO.getPatientVisitId(),
				medicalReviewDTO.getTenantId());
		patientVisit.setMedicalReview(true);
		patientVisitService.updatePatientVisit(patientVisit);

		if (!patientTracker.getPatientStatus().equals(Constants.ENROLLED)) {
			patientTracker.setPatientStatus(Constants.OBSERVATION);
			patientTracker.setIsObservation(Constants.BOOLEAN_TRUE);
		} else if (patientTracker.isRedRiskPatient()) {
			RedRiskService.updateRedRiskNoticationStatus(patientTracker.getId());
			patientTracker.setRedRiskPatient(Constants.BOOLEAN_FALSE);
		}

		PatientTreatmentPlan patientTreatmentPlan = patientTreatmentPlanService
				.getPatientTreatmentPlan(patientTracker.getId());

		if (!Objects.isNull(patientTreatmentPlan)) {
			Date nextMedicalReviewDate = patientTreatmentPlanService
					.getTreatmentPlanFollowupDate(patientTreatmentPlan.getMedicalReviewFrequency(), Constants.DEFAULT);
			patientTracker.setNextMedicalReviewDate(nextMedicalReviewDate);
		}
		patientTrackerService.addOrUpdatePatientTracker(patientTracker);

	}

	/**
	 * Creates a patient initial encounter from given patient
	 * 
	 * @param medicalReview  MedicalReview Request Data
	 * @param patientTracker PatientTracker
	 */
	public void createPatientInitialEncounter(MedicalReviewDTO medicalReview, PatientTracker patientTracker) {

		if (Constants.ENROLLED.equals(patientTracker.getPatientStatus())) {
			System.out.println("==========createPatientInitialEncounter");
			int comorbiditiesCount = medicalReview.getInitialMedicalReview().getComorbidities().stream()
					.filter(comorbidity -> Objects.isNull(comorbidity.getOtherComorbidity())
							|| comorbidity.getOtherComorbidity().equals(""))
					.collect(Collectors.toList()).size();
			System.out.println("comorbiditiesCPunt" + comorbiditiesCount);
			String diabetesDiagControlledType = Objects
					.isNull(medicalReview.getInitialMedicalReview().getDiagnosis().getDiabetesDiagControlledType()) ? ""
							: medicalReview.getInitialMedicalReview().getDiagnosis().getDiabetesDiagControlledType();
			System.out.println("diabetesDiagControlledType       " + diabetesDiagControlledType);
			String riskLevel = RedRiskService.getPatientRiskLevel(patientTracker,
					new RedRiskDTO(comorbiditiesCount, diabetesDiagControlledType,
							medicalReview.getInitialMedicalReview().getDiagnosis().getDiabetesPatientType()));
			if (!Objects.isNull(riskLevel) && "" != riskLevel) {
				patientTracker.setRiskLevel(riskLevel);
				// Update patient risk level. risk level
			}
			if (!Objects.isNull(medicalReview.getIsPregent())) {
				// Update pregnancy info in patient form
			}
			// update
		}
		createInitialMedicalReview(medicalReview);
		patientTracker.setInitialReview(true);
		patientTrackerService.addOrUpdatePatientTracker(patientTracker);

	}

	/**
	 * Creats initial medical review
	 * 
	 * @param medicalReview MedicalReview Request Data
	 */
	public void createInitialMedicalReview(MedicalReviewDTO medicalReview) {
		// Create initial medical review for the patient
		InitialMedicalReviewDTO initialMedicalReview = medicalReview.getInitialMedicalReview();
		if (!Objects.isNull(initialMedicalReview.getDiagnosis())) {
			initialMedicalReview.getDiagnosis().setPatientTrackId(medicalReview.getPatientTrackId());
			initialMedicalReview.getDiagnosis().setPatientVisitId(medicalReview.getPatientVisitId());
			initialMedicalReview.getDiagnosis().setTenantId(medicalReview.getTenantId());
			patientDiagnosisRepository.save(initialMedicalReview.getDiagnosis());
			// createPatientDiagnosis(initialMedicalReview.getDiagnosis(),
			// medicalReview.getPatientTrackId(),
			// medicalReview.getPatientVisitId(), medicalReview.getTenantId());
		}
		if (!Objects.isNull(initialMedicalReview.getComplications())
				&& !initialMedicalReview.getComplications().isEmpty()) {
			createPatientComplication(initialMedicalReview.getComplications(), medicalReview.getPatientTrackId(),
					medicalReview.getPatientVisitId(), medicalReview.getTenantId());
		}
		if (!Objects.isNull(initialMedicalReview.getComorbidities())
				&& !initialMedicalReview.getComorbidities().isEmpty()) {
			createPatientComorbidity(initialMedicalReview.getComorbidities(), medicalReview.getPatientTrackId(),
					medicalReview.getPatientVisitId(), medicalReview.getTenantId());
		}
		if (!Objects.isNull(initialMedicalReview.getLifestyle()) && !initialMedicalReview.getLifestyle().isEmpty()) {
			createPatientLifestyle(initialMedicalReview.getLifestyle(), medicalReview.getPatientTrackId(),
					medicalReview.getPatientVisitId(), medicalReview.getTenantId());
		}
		if (!Objects.isNull(initialMedicalReview.getCurrentMedications())
				&& !initialMedicalReview.getCurrentMedications().getMedications().isEmpty()) {
			createPatientCurrentMedication(initialMedicalReview.getCurrentMedications(),
					medicalReview.getPatientTrackId(), medicalReview.getPatientVisitId(), medicalReview.getTenantId());
		}
	}

	/**
	 * Create patientlifestyle for given track id and visit id
	 * 
	 * @param lifestyles     set of lifestyleDTO
	 * @param patientTrackId patientTrackId
	 * @param patientVisitId patientVisitId
	 * @param tenantId       tenantId
	 */
	private void createPatientLifestyle(Set<PatientLifestyle> lifestyles, Long patientTrackId, Long patientVisitId,
			Long tenantId) {

		for (PatientLifestyle patientLifestyle : lifestyles) {
			// patientComorbidity.setIds(tenantId, patientTrackId, patientVisitId);
			patientLifestyle.setTenantId(tenantId);
			patientLifestyle.setPatientTrackId(patientTrackId);
			patientLifestyle.setPatinetVisitId(patientVisitId);
		}
		// Set<PatientLifestyle> patientLifestyles = lifestyles.stream().map(
		// lifestyle -> new PatientLifestyle(tenantId, patientTrackId, patientVisitId,
		// lifestyle.getLifestyleId(),
		// lifestyle.getLifestyleAnswer(), lifestyle.getComments()))
		// .collect(Collectors.toSet());

		patientLifestyleRepository.saveAll(lifestyles);

	}

	/**
	 * Create PatientComorbidity for given track id and visit id
	 * 
	 * @param comorbidities  set of ComorbidityDTO
	 * @param patientTrackId patientTrackId
	 * @param patientVisitId patientVisitId
	 * @param tenantId       tenantId
	 */
	private void createPatientComorbidity(Set<PatientComorbidity> comorbidities, Long patientTrackId,
			Long patientVisitId, Long tenantId) {

		for (PatientComorbidity patientComorbidity : comorbidities) {
			// patientComorbidity.setIds(tenantId, patientTrackId, patientVisitId);
			patientComorbidity.setTenantId(tenantId);
			patientComorbidity.setPatientTrackId(patientTrackId);
			patientComorbidity.setPatientVisitId(patientVisitId);
		}
		// Set<PatientComorbidity> patientComorbidities = comorbidities.stream().map(
		// comorbidity -> new PatientComorbidity(tenantId, patientTrackId,
		// patientVisitId,
		// comorbidity.getComorbidityId(), comorbidity.getOtherComorbity()))
		// .collect(Collectors.toSet());

		patientComorbidityRepository.saveAll(comorbidities);
	}

	/**
	 * Create PatientComplication for given track id and visit id
	 * 
	 * @param complications  set of ComplicationDTO
	 * @param patientTrackId patientTrackId
	 * @param patientVisitId patientVisitId
	 * @param tenantId       tenantId
	 */
	private void createPatientComplication(Set<PatientComplication> complications, Long patientTrackId,
			Long patientVisitId, Long tenantId) {

		for (PatientComplication patientComplication : complications) {
			// patientComplication.setIds(tenantId, patientTrackId, patientVisitId);
			patientComplication.setTenantId(tenantId);
			patientComplication.setPatientTrackId(patientTrackId);
			patientComplication.setPatinetVisitId(patientVisitId);
		}
		patientComplicationRepository.saveAll(complications);
	}

	/**
	 * Create PatientDiagnosis for given track id and visit id
	 * 
	 * @param diagnosis      DiagnosisDTO
	 * @param patientTrackId patientTrackId
	 * @param patientVisitId patientVisitId
	 * @param tenantId       tenantId
	 */
	public void createPatientDiagnosis(DiagnosisDTO diagnosis, Long patientTrackId, Long patientVisitId,
			Long tenantId) {

		PatientDiagnosis patientDiagnosis = new PatientDiagnosis(tenantId, patientTrackId, patientVisitId,
				diagnosis.getHtnYearOfDiagnosis(), diagnosis.getDiabetesYearOfDiagnosis(),
				diagnosis.getHtnPatientType(), diagnosis.getDiabetesPatientType(), diagnosis.getDiabetesDiagnosis(),
				diagnosis.getDiabetesDiagControlledType(), diagnosis.getIsHtnDiagnosis(),
				diagnosis.getIsDiabetesDiagnosis());

		patientDiagnosisRepository.save(patientDiagnosis);
	}

	/**
	 * Create PatientCurrentMedication for given track id and visit id
	 * 
	 * @param currentMedication CurrentMedicationDetailsDTO
	 * @param patientTrackId    patientTrackId
	 * @param patientVisitId    patientVisitId
	 * @param tenantId          tenantId
	 */
	public void createPatientCurrentMedication(CurrentMedicationDetailsDTO currentMedication, Long patientTrackId,
			Long patientVisitId, Long tenantId) {

		Set<PatientCurrentMedication> patientCurrentMedications = currentMedication.getMedications().stream()
				.map(medication -> new PatientCurrentMedication(tenantId, medication.getCurrentMedicationId(),
						patientTrackId, patientVisitId, currentMedication.isDrugAllergies(),
						currentMedication.isAdheringCurrentMed(), currentMedication.getAdheringMedComment(),
						currentMedication.getAllergiesComment()))
				.collect(Collectors.toSet());
		patientCurrentMedicationRepository.saveAll(patientCurrentMedications);
	}

	/**
	 * {@inheritDoc}
	 */
	public MedicalReviewResponseDTO getMedicalReviewHistory(RequestDTO medicalReviewListDTO) {
		if (Objects.isNull(medicalReviewListDTO.getPatientTrackId())) {
			throw new DataNotAcceptableException(10010);
		}
		List<PatientVisit> patientVisitList = new ArrayList<>();
		// List<Date> visitDates = null; //= new ArrayList<>();
		List<Map<String, Object>> visits = new ArrayList<>();
		if (medicalReviewListDTO.isLatestRequired() && Objects.isNull(medicalReviewListDTO.getPatientVisitId())) {
			patientVisitList = patientVisitService.getPatientVisitDates(medicalReviewListDTO.getPatientTrackId(), null,
					Constants.BOOLEAN_TRUE, null);

			medicalReviewListDTO.setPatientVisitId(
					0 != patientVisitList.size() ? patientVisitList.get(patientVisitList.size() - 1).getId() : null);

			System.out.println("visitsss      " + visits.size());

			for (PatientVisit visit : patientVisitList) {
				visits.add(Map.of("id", visit.getId(), "visitDate", visit.getVisitDate()));
			}
			System.out.println("visitsss   in loop   " + visits.size());

			// visitDates = patientVisitList.stream().map(
			// visit -> visit.getVisitDate()).collect(Collectors.toList());
			// System.out.println("visit dated ====" + visitDates);
		}
		List<PatientMedicalReview> patientMedicalReviews = medicalReviewRepository.getPatientMedicalReview(
				medicalReviewListDTO.getPatientTrackId(), medicalReviewListDTO.getPatientVisitId());
		List<PatientMedicalReviewDTO> patientMedicalReviewDTOs = constructResponseDTO(patientMedicalReviews);
		return new MedicalReviewResponseDTO(visits, patientMedicalReviewDTOs);
	}

	public MedicalReviewResponseDTO getMedicalReviewSummaryHistory(RequestDTO medicalReviewListDTO) {
		Boolean isMedicalReview;
		Boolean isInvestigation;
		Boolean isPrescription;
		List<PatientVisit> patientVisitList = new ArrayList<>();
		List<Map<String, Object>> visits = new ArrayList<>();
		PatientVisit latestPatientVisit = null;
		if (Objects.isNull(medicalReviewListDTO.getPatientTrackId())) {
			throw new DataNotAcceptableException(10010);
		}
		if (medicalReviewListDTO.isLatestRequired()) {
			isMedicalReview = true;
			isInvestigation = true;
			isPrescription = true;
			if (medicalReviewListDTO.isMedicalReviewSummary()) {
				isMedicalReview = true;
				isInvestigation = null;
				isPrescription = null;
			}
			patientVisitList = patientVisitService.getPatientVisitDates(medicalReviewListDTO.getPatientTrackId(),
					isInvestigation, isMedicalReview, isPrescription);
			// visitDates = patientVisitList.stream().map(
			// visit -> visit.getVisitDate()).collect(Collectors.toList());
			// visit dates
			if (patientVisitList.isEmpty()) {
				// Set patient visit Id
				return new MedicalReviewResponseDTO();
			} else {
				patientVisitList.stream()
						.map(visit -> visits.add(Map.of("id", visit.getId(), "visitDate", visit.getId())));
				latestPatientVisit = patientVisitList.get(patientVisitList.size() - 1);
				medicalReviewListDTO.setPatientVisitId(latestPatientVisit.getId());
			}
		}
		if (Objects.isNull(medicalReviewListDTO.getPatientVisitId())) {
			throw new DataNotAcceptableException();
		} else if (Objects.isNull(latestPatientVisit)) {
			latestPatientVisit = patientVisitService.getPatientVisitById(medicalReviewListDTO.getPatientVisitId());
		}

		MedicalReviewResponseDTO medicalReviewSummary = getMedicalReviewSummary(medicalReviewListDTO,
				latestPatientVisit);
		medicalReviewSummary.setPatientReviewDates(visits);
		if (medicalReviewListDTO.isDetailedSummaryRequired()) {

		}
		return medicalReviewSummary;

	}

	private MedicalReviewResponseDTO getMedicalReviewSummary(RequestDTO medicalReviewListDTO,
			PatientVisit patientVisit) {
		MedicalReviewResponseDTO medicalReviewResponse = new MedicalReviewResponseDTO();

		if (patientVisit.isMedicalReview()) {
			List<PatientMedicalReview> patientMedicalReviews = medicalReviewRepository.getPatientMedicalReview(
					medicalReviewListDTO.getPatientTrackId(), medicalReviewListDTO.getPatientVisitId());
			medicalReviewResponse.setMedicalReviews(constructResponseDTO(patientMedicalReviews));
		} else {
			medicalReviewResponse.setMedicalReviews(new ArrayList<>());
		}
		if (patientVisit.isInvestigation()) {
			medicalReviewResponse.setInvestigations(patientLabTestService
					.getPatientLabTest(medicalReviewListDTO.getPatientTrackId(), patientVisit.getId()));
		} else {
			medicalReviewResponse.setInvestigations(new ArrayList<>());
		}
		if (medicalReviewListDTO.isDetailedSummaryRequired()) {
			// To get recent visit prescription details, prefer to use prescription instead
			// of prescription history collection
			if (patientVisit.isPrescription()) {
				List<Prescription> prescriptions = prescriptionRepository
						.findByPatientTrackIdAndPatientVisitIdAndIsDeleted(medicalReviewListDTO.getPatientTrackId(),
								patientVisit.getId(), false);
				medicalReviewResponse.setPrescriptions(prescriptions);
				medicalReviewResponse.setIsSigned(!prescriptions.isEmpty());
			}
			PatientTracker patientTracker = patientTrackerService
					.getPatientTrackerById(medicalReviewListDTO.getPatientTrackId());

			medicalReviewResponse.setPatientDetails(Map.of("provisional_diagnosis",
					patientTracker.getProvisionalDiagnosis(), "confirm_diagnosis", patientTracker.getConfirmDiagnosis(),
					"is_confirm_diagnosis", patientTracker.getIsConfirmDiagnosis()));

			PatientTreatmentPlan treatmentPlan = patientTreatmentPlanService.getPatientTreatmentPlanDetails(
					medicalReviewListDTO.getPatientTrackId(), medicalReviewListDTO.getTenantId());
			if (!Objects.isNull(treatmentPlan)) {
				medicalReviewResponse.setMedicalReviewFrequency(treatmentPlan.getMedicalReviewFrequency());
			}
			medicalReviewResponse.setReviewedAt(patientVisit.getCreatedAt());

			// TODO : reviewer details from user model
		}
		if (!medicalReviewListDTO.isDetailedSummaryRequired() && patientVisit.isPrescription()) {
			List<PrescriptionHistory> histories = getPrescriptionHistory(patientVisit);
			medicalReviewResponse.setPrescriptions(histories);
			// medicalReviewResponse.setPrescriptions(getPrescriptionHistory(patientVisit));
		}

		return medicalReviewResponse;

	}

	private List<PatientMedicalReviewDTO> constructResponseDTO(List<PatientMedicalReview> patientMedicalReviews) {
		List<PatientMedicalReviewDTO> patientMedicalReviewDTOs = new ArrayList<>();
		if (!Objects.isNull(patientMedicalReviews)) {
			for (PatientMedicalReview patientMedicalReview : patientMedicalReviews) {
				Set<String> physicalExams = patientMedicalReview.getPhysicalExams().stream()
						.map(physicalExam -> physicalExam.getName()).collect(Collectors.toSet());
				Set<String> complaints = patientMedicalReview.getComplaints().stream()
						.map(complaint -> complaint.getName()).collect(Collectors.toSet());
				patientMedicalReviewDTOs.add(new PatientMedicalReviewDTO(patientMedicalReview.getId(),
						patientMedicalReview.getPatientVisitId(), physicalExams, complaints,
						patientMedicalReview.getPhysicalExamComments(), patientMedicalReview.getCompliantComments(),
						patientMedicalReview.getCreatedAt()));
			}
		}
		return patientMedicalReviewDTOs;
	}

	private List<PrescriptionHistory> getPrescriptionHistory(PatientVisit patientVisit) {
		return prescriptionHistoryRepository.getPrescriptions(patientVisit.getId());

	}

	public Map<String, Integer> getPrescriptionAndLabtestCount(RequestDTO request) {
		if (!Objects.isNull(request.getPatientTrackId())) {
			throw new SpiceValidation();
		}
		int prescriptionDaysCompletedCount = prescriptionRepository.getPrecriptionCount(new Date(),
				request.getPatientTrackId());
		int nonReviewedTestCount = patientLabTestService.getLabtestCount(request.getPatientTrackId());
		int nutritionLifestyleReviewedCount = lifestyleRepository
				.nutritionLifestyleReviewedCount(request.getPatientTrackId());
		return Map.of("prescriptionDaysCompletedCount", prescriptionDaysCompletedCount, "nonReviewedTestCount",
				nonReviewedTestCount, "nutritionLifestyleReviewedCount", nutritionLifestyleReviewedCount);
	}

}
