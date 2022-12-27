package com.mdtlabs.coreplatform.spiceservice.staticdata.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.contexts.UserContextHolder;
import com.mdtlabs.coreplatform.common.exception.Validation;
import com.mdtlabs.coreplatform.common.model.dto.UserDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.CustomizationDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.MedicalReviewStaticDataDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SiteDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.StaticDataDTO;
import com.mdtlabs.coreplatform.common.model.entity.Account;
import com.mdtlabs.coreplatform.common.model.entity.Site;
import com.mdtlabs.coreplatform.common.model.entity.spice.AccountCustomization;
import com.mdtlabs.coreplatform.common.model.entity.spice.AccountWorkflow;
import com.mdtlabs.coreplatform.common.model.entity.spice.Frequency;
import com.mdtlabs.coreplatform.common.model.entity.spice.ModelQuestions;
import com.mdtlabs.coreplatform.common.model.entity.spice.RegionCustomization;
import com.mdtlabs.coreplatform.spiceservice.ApiInterface;
import com.mdtlabs.coreplatform.spiceservice.common.repository.NutritionLifestyleRepository;
import com.mdtlabs.coreplatform.spiceservice.frequency.service.FrequencyService;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.DiagnosisRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.DosageFormRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.MedicalComplianceRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.ModelQuestionsRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.ReasonRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.RiskAlgorithmRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.SideMenuRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.UnitRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.ComorbidityService;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.ComplaintsService;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.ComplicationService;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.CurrentMedicationService;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.LifestyleService;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.PhysicalExaminationService;
import com.mdtlabs.coreplatform.spiceservice.staticdata.service.StaticDataService;
import com.mdtlabs.coreplatform.spiceservice.symptom.repository.SymptomRepository;

/**
 * 
 * This service implements the StaticDataService and is responsible for
 * implementing business logic needed to get meta data related to the service.
 *
 * @author Rajkumar
 */
@Service
public class StaticDataServiceImpl implements StaticDataService {
	@Autowired
	private DosageFormRepository dosageFormRepository;
	@Autowired
	private UnitRepository unitRepository;
	@Autowired
	private NutritionLifestyleRepository nutritionLifestyleRepository;
	@Autowired
	private SymptomRepository symptomRepository;
	@Autowired
	private MedicalComplianceRepository medicalComplianceRepository;
	@Autowired
	private DiagnosisRepository diagnosisRepository;
	@Autowired
	private RiskAlgorithmRepository riskAlgorithmRepository;
	@Autowired
	private ReasonRepository reasonRepository;
	@Autowired
	private SideMenuRepository sideMenuRepository;
	@Autowired
	private ApiInterface apiInterface;
	@Autowired
	private ComorbidityService comorbidityService;
	@Autowired
	private ComplicationService complicationService;
	@Autowired
	private LifestyleService lifestyleService;
	@Autowired
	private PhysicalExaminationService physicalExaminationService;
	@Autowired
	private ComplaintsService complaintsService;
	@Autowired
	private CurrentMedicationService currentMedicationService;
	@Autowired
	private FrequencyService frequencyService;
	@Autowired
	private ModelQuestionsRepository modelQuestionsRepository;

	private ModelMapper mapper = new ModelMapper();

	/**
	 * {@inheritDoc}
	 */
	public StaticDataDTO getStaticData() {
		StaticDataDTO response = new StaticDataDTO();
		UserDTO userDTO = UserContextHolder.getUserDto();
		Long countryId = userDTO.getCountry().getId();
		String token = Constants.BEARER + userDTO.getAuthorization();
		Long tenantId = userDTO.getTenantId();

		List<Long> tenants = List.of(1L, 2L, 3L, 4L);
//		Long userTenantId = Long.parseLong(UserSelectedTenantContextHolder.get().toString());
//		List<Long> tenants = UserTenantsContextHolder.get().stream().map(a -> Long.parseLong(a.toString()))
//				.collect(Collectors.toList());
		List<Site> sites = apiInterface.getSitesByTenantIds(token, tenantId, tenants);
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		List<SiteDTO> siteDTOList = mapper.map(sites, new TypeToken<List<SiteDTO>>() {
		}.getType());
		Account account = apiInterface.getAccountById(token, tenantId, sites.get(0).getAccountId());
		List<AccountWorkflow> workflows = apiInterface.getAllAccountWorkFlows(token, tenantId);
		List<AccountWorkflow> clinicalWorkflows = account.getClinicalWorkflows();

		List<AccountWorkflow> customizedWorkflows = account.getCustomizedWorkflows();
		Map<String, Boolean> clinicalWorkflowResponse = new HashMap<>();
		for (AccountWorkflow accountWorkflow : clinicalWorkflows) {
			clinicalWorkflowResponse.put(accountWorkflow.getWorkflow(), true);
		}
		if (!Objects.isNull(customizedWorkflows) && !customizedWorkflows.isEmpty()) {
			Map<String, Boolean> customizedWorkflowResponse = new HashMap<>();
			Map<String, Boolean> customizedWorkflow = new HashMap<>();
			for (AccountWorkflow accountWorkflow : customizedWorkflows) {
				customizedWorkflow.put(accountWorkflow.getWorkflow(), true);
			}
			response.setCustomizedWorkflow(customizedWorkflowResponse);
		}
		List<String> userRoles = userDTO.getRoles().stream().map(role -> role.getName()).collect(Collectors.toList());
		List<String> roleDisplayNames = userDTO.getRoles().stream().map(role -> role.getName())
				.collect(Collectors.toList());
		for (SiteDTO siteDto : siteDTOList) {
			siteDto.setRoleName(userRoles);
			siteDto.setDisplayName(roleDisplayNames);
		}

		List<Long> siteIds = sites.stream().map(Site::getId).collect(Collectors.toList());
		response.setPrograms(apiInterface.getPrograms(token, tenantId, siteIds));
		response.setMenus(sideMenuRepository.findByRoleNameIn(userRoles));

		response.setClinicalWorkflow(clinicalWorkflowResponse);
		response.setAccountId(account.getId());
		response.setOperatingUnitId(sites.get(0).getOperatingUnitId());
		response.setSites(siteDTOList);

		response.setDefaultSite(
				sites.stream().filter(site -> site.getTenantId() == UserContextHolder.getUserDto().getTenantId())
						.findAny().orElse(null));
		response.setOperatingSites(
				apiInterface.getSitesByOperatingUnitId(token, tenantId, sites.get(0).getOperatingUnitId()));

		setMetaData(response, countryId, token, tenantId);
		getAccountCustomization(countryId, response, token, tenantId);
		return response;
	}

	/**
	 * Sets meta data like DosageForms, Units, etc.,
	 * 
	 * @param response  Object containing meta data.
	 * @param countryId user country id
	 */
	private void setMetaData(StaticDataDTO response, long countryId, String token, long tenantId) {
		response.setDosageForm(dosageFormRepository.findByNameNotLike(Constants.OTHER));
		response.setUnits(unitRepository.findByNameNotLike(Constants.OTHER));
		response.setNutritionLifestyle(
				nutritionLifestyleRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder")));
		response.setSymptoms(symptomRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder")));
		response.setMedicalCompliances(
				medicalComplianceRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder")));
		response.setDiagnosis(diagnosisRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder")));
		response.setCvdRiskAlgorithms(
				(riskAlgorithmRepository.findByCountryId(countryId, Sort.by(Sort.Direction.ASC, "countryId"))).get(0)
						.getRiskAlgorithm());
		response.setReasons(reasonRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder")));
		response.setCountries(List.of(apiInterface.getCountryById(token, tenantId, countryId)));
		response.setCounties(apiInterface.getAllCountyByCountryId(token, tenantId, countryId));
		response.setSubcounties(apiInterface.getAllSubCountyByCountryId(token, tenantId, countryId));
		response.setMentalHealth(getMentalHealthStaticData(countryId));
	}

	/**
	 * To get account or region based workflow.
	 * 
	 * @param countryId user country ID
	 * @param response  object containing meta data.
	 * @return
	 */
	private List<AccountCustomization> getAccountCustomization(Long countryId, StaticDataDTO response, String token,
			long tenantId) {
		List<AccountCustomization> accountCustomizations = new ArrayList<>();
//		List<CustomizationDTO> workflowsCutomizationDTOs = new ArrayList<>();
		List<String> screenTypes = List.of(Constants.WORKFLOW_ENROLLMENT, Constants.WORKFLOW_SCREENING,
				Constants.WORKFLOW_ASSESSMENT, Constants.MODULE);
		List<String> category = List.of(Constants.INPUT_FORM, Constants.CONSENT_FORM);
		accountCustomizations = apiInterface.getAccountCustomization(token, tenantId,
				Map.of("screenTypes", screenTypes, "category", category, "countryId", countryId));
		List<AccountCustomization> accountConsentForms = new ArrayList<>();
		List<AccountCustomization> accountCustomizedModules = new ArrayList<>();

		if (null != accountCustomizations && !accountCustomizations.isEmpty()) {
			accountConsentForms = accountCustomizations.stream().filter(form -> (form != null
					&& form.getAccountId() == countryId && form.getCategory().equals(Constants.CONSENT_FORM)))
					.collect(Collectors.toList());
			accountCustomizedModules = accountCustomizations.stream()
					.filter(form -> (form != null && form.getCategory().equals(Constants.MODULE)))
					.collect(Collectors.toList());
		}
		if (!accountConsentForms.isEmpty()) {

			List<String> accountConsentFormTypes = accountConsentForms.stream().map(AccountCustomization::getType)
					.collect(Collectors.toList());
			accountConsentForms.stream().map(data -> {
				return new CustomizationDTO(data.getType(), data.getCategory(), data.getFormInput(),
						data.getCountryId(), data.getTenantId());
			}).collect(Collectors.toList());
			screenTypes.remove(Constants.MODULE);
			screenTypes.removeAll(accountConsentFormTypes);
		}
		List<String> regionCustomizationTypes = new ArrayList<>(screenTypes);

		List<String> regionConsentFormTypes = screenTypes;

		Map<String, Object> requestData = Map.of("regionCustomizationTypes", regionCustomizationTypes,
				"regionConsentFormTypes", category);
		Map<String, String> enrollment = new HashMap<>();
		Map<String, String> screening = new HashMap<>();
		Map<String, String> assessment = new HashMap<>();
		if (!regionConsentFormTypes.isEmpty()) {
			List<RegionCustomization> regionCustomizations = apiInterface.getRegionCustomizations(token, tenantId,
					requestData);
//			workflowsCutomizationDTOs.addAll(
			if (!Objects.isNull(regionCustomizations) && !regionCustomizations.isEmpty()) {
				for (RegionCustomization customization : regionCustomizations) {

//				return new CustomizationDTO(data.getType(), data.getCategory(), data.getFormInput(),
//						data.getCountryId(), data.getTenantId());
					if (customization.getType().equalsIgnoreCase(Constants.SCREENING)) {
						screening.put(customization.getCategory(), customization.getFormInput());
					}

					if (customization.getType().equalsIgnoreCase(Constants.ENROLLMENT)) {
						enrollment.put(customization.getCategory(), customization.getFormInput());
					}

					if (customization.getType().equalsIgnoreCase(Constants.ASSESSMENT)) {
						assessment.put(customization.getCategory(), customization.getFormInput());
					}
				}
			}
		}
		response.setEnrollment(enrollment);
		response.setAssessment(assessment);
		response.setScreening(screening);
		return accountCustomizations;
	}

	/**
	 * Gets mental health meta data for a particular country.
	 * 
	 * @param countryId country ID of the user.
	 * @return Collection on mental health meta data.
	 */
	public List<Map<String, String>> getMentalHealthStaticData(Long countryId) {
		List<ModelQuestions> modelQuestions = modelQuestionsRepository.findByCountryIdAndIsDeleted(countryId, false);
		if (Objects.isNull(modelQuestions) || modelQuestions.isEmpty()) {
			modelQuestions = modelQuestionsRepository.findByIsDefaultAndIsDeleted(true, false);
		}
		ObjectMapper mapper = new ObjectMapper();

		Map<String, List<ModelQuestions>> questions = new HashMap<>();
		List<Map<String, String>> response = new ArrayList<>();

		for (ModelQuestions question : modelQuestions) {
			if (questions.keySet().contains(question.getType())) {
				questions.get(question.getType()).add(question);
			} else {
				questions.put(question.getType(), new ArrayList<ModelQuestions>());
				questions.get(question.getType()).add(question);
			}
		}

		for (List<ModelQuestions> question : questions.values()) {
			try {
				response.add(Map.of(Constants.TYPE, question.get(Constants.ZERO).getType(), Constants.QUESTIONS,
						mapper.writeValueAsString(question)));
			} catch (JsonProcessingException e) {
				throw new Validation(1001);
			}
		}
		return response;
	}

	/**
	 * {@inheritDoc}
	 */
	public MedicalReviewStaticDataDTO getMedicalReviewStaticData() {
		MedicalReviewStaticDataDTO response = new MedicalReviewStaticDataDTO();
		response.setComorbidity(comorbidityService.findByIsDeletedFalseAndIsActiveTrue());
		response.setComplaints(complaintsService.findByIsDeletedFalseAndIsActiveTrue());
		response.setCurrentMedication(currentMedicationService.findByIsDeletedFalseAndIsActiveTrue());
		response.setComplications(complicationService.findByIsDeletedFalseAndIsActiveTrue());
		response.setPhysicalExamination(physicalExaminationService.findByIsDeletedFalseAndIsActiveTrue());
		response.setLifestyle(lifestyleService.findByIsDeletedFalseAndIsActiveTrue());
		List<Frequency> frequencies = frequencyService.findByIsDeletedFalseAndIsActiveTrue();
		List<Map<String, String>> forms = Constants.FREQUENCIES.values().stream().map(frequency -> Map.of("label_name",
				frequency.get("LABEL"), "frequency_key", frequency.get("FREQUENCY_KEY"))).collect(Collectors.toList());

		List<String> options = frequencies.stream().filter(frequency -> frequency.getType().equals(Constants.DEFAULT))
				.map(Frequency::getName).collect(Collectors.toList());
		response.setTreatmentPlanFromData(Map.of(Constants.FORMS, forms, Constants.OPTIONS, options));
		return response;
	}
}
