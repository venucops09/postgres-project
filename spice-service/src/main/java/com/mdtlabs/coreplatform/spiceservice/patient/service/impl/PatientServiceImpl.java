package com.mdtlabs.coreplatform.spiceservice.patient.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.Validator;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.UnitConstants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataConflictException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.dto.spice.BioDataDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.EnrollmentRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.EnrollmentResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.GetRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.GlucoseLogDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientGetRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientTrackerDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PregnancyRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RedRiskDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.BpLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.GlucoseLog;
import com.mdtlabs.coreplatform.common.model.entity.spice.MentalHealth;
import com.mdtlabs.coreplatform.common.model.entity.spice.Patient;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientPregnancyDetails;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.common.model.entity.spice.ScreeningLog;
import com.mdtlabs.coreplatform.common.util.CommonUtil;
import com.mdtlabs.coreplatform.common.util.UnitConversion;
import com.mdtlabs.coreplatform.spiceservice.bplog.service.BpLogService;
import com.mdtlabs.coreplatform.spiceservice.common.RedRiskService;
import com.mdtlabs.coreplatform.spiceservice.customizedmodules.service.CustomizedModulesService;
import com.mdtlabs.coreplatform.spiceservice.glucoseLog.service.GlucoseLogService;
import com.mdtlabs.coreplatform.spiceservice.mentalhealth.service.MentalHealthService;
import com.mdtlabs.coreplatform.spiceservice.patient.repository.PaitentPregnancyDetailsRepository;
import com.mdtlabs.coreplatform.spiceservice.patient.repository.PatientRepository;
import com.mdtlabs.coreplatform.spiceservice.patient.service.PatientService;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;
import com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.service.PatientTreatmentPlanService;
import com.mdtlabs.coreplatform.spiceservice.screeningLog.service.ScreeningLogService;


/**
 * This is the class that implements PatientService class and contains the
 * actual business logic for Patient Entity.
 *
 * @author Niraimathi S
 */
@Service
public class PatientServiceImpl implements PatientService {
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private PatientTrackerService patientTrackerService;
    @Autowired
    private PatientTreatmentPlanService patientTreatmentPlanService;
    @Autowired
    private BpLogService bpLogService;
    @Autowired
    private GlucoseLogService glucoseLogService;
    @Autowired
    private ScreeningLogService screeningLogService;
    @Autowired
    private PaitentPregnancyDetailsRepository pregnancyDetailsRepository;
    @Autowired
    private CustomizedModulesService customizedModulesService;

    @Autowired 
	private MentalHealthService mentalHealthService;


    /**
     * {@inheritDoc}
     */
    public EnrollmentResponseDTO createPatient(EnrollmentRequestDTO data) {
        // TODO: Site validation
//        validateRequestData(data);

        String searchNationalId = data.getBioData().getNationalId().replaceAll("[^a-zA-Z0-9]*", "");
        PatientTracker existingPatientTracker = (!Objects.isNull(data.getPatientTrackerId()))
                ? patientTrackerService.getPatientTrackerById(data.getPatientTrackerId())
                : patientTrackerService.findByNationalIdIgnoreCase(searchNationalId);

        if (!Objects.isNull(existingPatientTracker) && existingPatientTracker.getPatientStatus().equals("ENROLLED")) {
            throw new DataConflictException(3005); // patient Already Enrolled
        }

        Patient patient = constructPatientData(data.getBioData(), data);
        Validator validator;
//        validator.validate(patient, );
        Patient enrolledPatient = patientRepository.save(patient);
        PatientTracker patientTracker = null;
        String riskLevel = null;
        if (!Objects.isNull(existingPatientTracker)) {
            patientTracker = constructPatientTracker(enrolledPatient, data,
                    existingPatientTracker);
        } else {
            patientTracker = constructPatientTracker(enrolledPatient, data, new PatientTracker());
            patientTracker = patientTrackerService.addOrUpdatePatientTracker(patientTracker);
        }

        if (!Objects.isNull(data.getPhq4())) {
            MentalHealth mentalHealth = data.getPhq4();
            mentalHealthService.setPHQ4Score(mentalHealth);
            mentalHealth.setPatientTrackId(patientTracker.getId());
            patientTracker.setPhq4RiskLevel(mentalHealth.getPhq4RiskLevel());
            patientTracker.setPhq4Score(mentalHealth.getPhq4Score());
            patientTracker.setPhq4FirstScore(mentalHealth.getPhq4FirstScore());
            patientTracker.setPhq4SecondScore(mentalHealth.getPhq4SecondScore());
            mentalHealthService.createMentalHealth(mentalHealth);
        }

        if (!Objects.isNull(patientTracker) && patientTracker.isInitialReview() == Constants.BOOLEAN_TRUE) {
            RedRiskDTO redRiskDTO = new RedRiskDTO();
            // TODO : convert glucose unit from MgDl to Mmol
//            if (data.getGlucoseLog().getGlucoseUnit().equals(Constants.MG_DL)) {
//                patientTracker.setGlucoseValue(UnitConversion.convertMgDlToMmol(data.getGlucoseLog().getGlucoseValue()));
//            }
            riskLevel = RedRiskService.getPatientRiskLevel(patientTracker, redRiskDTO);
        }

        if (!Objects.isNull(riskLevel)) {
            patientTracker.setRiskLevel(riskLevel);
        }
        BpLog bpLog = constructBpLogData(data);
        bpLog = bpLogService.addBpLog(bpLog, Constants.BOOLEAN_FALSE);
        GlucoseLog glucoseLog = null;
        if (!Objects.isNull(data.getGlucoseLog())) {
            glucoseLog = constructGlucoseLogData( data);
            glucoseLog.setPatientTrackId(patientTracker.getId());
            glucoseLog = glucoseLogService.addGlucoseLog(glucoseLog, Constants.BOOLEAN_FALSE);
        }

        List<Map<String, String>> treatmentPlanDurations = patientTreatmentPlanService.createProvisionalTreatmentPlan(
                patientTracker,
                data.getCvdRiskLevel(), patient.getTenantId());

        if (!Objects.isNull(data.getCustomizedWorkflows()) && !data.getCustomizedWorkflows().isEmpty()) {
            customizedModulesService.createCustomizedModules(data.getCustomizedWorkflows(), Constants.WORKFLOW_ENROLLMENT,
                    patientTracker.getId());
        }
//      TODO: OutboundSMS
//        patientTrackerService.addOrUpdatePatientTracker(patientTracker);
        EnrollmentResponseDTO response = new EnrollmentResponseDTO();
        response.setEnrollment(enrolledPatient);
        response.setBpLog(bpLog);
        response.setGlucoseLog(glucoseLog);
        response.setTreatmentPlan(treatmentPlanDurations);
        return response;
    }

    /**
     * Validates enrollment request data.
     *
     * @param requestData Request data
     * @author Niraimathi S
     */
    private void validateRequestData(EnrollmentRequestDTO requestData) {
        if (!Objects.isNull(requestData.getBplog()) && (2 > requestData.getBplog().getBpLogDetails().size())) {
            throw new BadRequestException(8001);
        }
    }

    /**
     * This method constructs Patient object data to enroll a new patient.
     *
     * @param bioData BioData DTO with patient bio data info
     * @param data    Request data with necessary data
     * @return Patient entity
     * @author Niraimathi S
     */
    private Patient constructPatientData(BioDataDTO bioData, EnrollmentRequestDTO data) {
        Patient patient = new Patient();
        if (!Objects.isNull(bioData)) {
            patient.setNationalId(bioData.getNationalId());
            patient.setFirstName(bioData.getFirstName());
            patient.setMiddleName(bioData.getMiddleName());
            patient.setLastName(bioData.getLastName());
            patient.setGender(data.getGender());
            patient.setPhoneNumber(bioData.getPhoneNumber());
            patient.setPhoneNumberCategory(bioData.getPhoneNumberCategory());
            patient.setCountryId(bioData.getCountry());
            patient.setCountyId(bioData.getCounty());
            patient.setSubCountyId(bioData.getSubCounty());
            patient.setLandmark(bioData.getLandmark());
            patient.setOccupation(bioData.getOccupation());
            patient.setLevelOfEducation(bioData.getLevelOfEducation());
            patient.setInsuranceStatus(bioData.getInsuranceStatus());
            patient.setInsuranceType(bioData.getInsuranceType());
            patient.setInsuranceId(bioData.getInsuranceId());
            patient.setProgramId(bioData.getProgram());
            patient.setInitial(bioData.getInitial());
            patient.setSiteId(data.getSite());
            patient.setTenantId(data.getTenantId());
            // patient.setOtherInsurance();
            // patient.setIsSupportGroup();
            patient.setRegularSmoker(data.getIsRegularSmoker());
            patient.setDateOfBirth(data.getDateOfBirth());
            patient.setAge(data.getAge());
            patient.setIsPregnant(data.getIsPregnant());
        }
        return patient;
    }

    /**
     * Construct a new GlucoseLog data.
     *
     * @param data          Request data with glucose values
     * @return GlucoseLog Entity
     * @author Niraimathi S
     */
    private GlucoseLog constructGlucoseLogData(EnrollmentRequestDTO data) {
        GlucoseLog glucoseLog = data.getGlucoseLog();
        if (!Objects.isNull(glucoseLog) && (!Objects.isNull(glucoseLog.getGlucoseValue()) || !Objects.isNull(glucoseLog.getHb1ac()))) {
            if (!Objects.isNull(glucoseLog.getGlocoseLogId())) {
                glucoseLog.setId(glucoseLog.getGlocoseLogId());
                glucoseLog.setUpdatedFromEnrollment(Constants.BOOLEAN_TRUE);
            }
            glucoseLog.setTenantId(data.getTenantId());
            glucoseLog.setType(Constants.ENROLLMENT);
        }
        return glucoseLog;
    }

    /**
     * Constructs a new BPLog data from request data.
     *
     * @param data     request data with BpLog data
     * @return BpLog Entity
     * @author Niraimathi S
     */
    private BpLog constructBpLogData(EnrollmentRequestDTO data) {
        BpLog bpLog = data.getBplog();
        if(!Objects.isNull(bpLog)) {
            if (!Objects.isNull(bpLog.getBpLogId())) {
                bpLog.setId(bpLog.getBpLogId());
                bpLog.setUpdatedFromEnrollment(Constants.BOOLEAN_TRUE);
            }
            bpLog.setCvdRiskLevel(data.getCvdRiskLevel());
            bpLog.setCvdRiskScore(data.getCvdRiskScore());
            // bpLog.setIsLatest();
            bpLog.setRegularSmoker(data.getIsRegularSmoker());
            bpLog.setType(Constants.ENROLLMENT);
        }
        return bpLog;
    }

    /**
     * Construct a patientTracker object and add or update it.
     *
     * @param enrolledPatient patient data
     * @param requestData     request data containing necessary fields
     * @return PatientTracker Entity
     * @author Niraimathi S
     */
    public PatientTracker constructPatientTracker(Patient enrolledPatient,
                                                  EnrollmentRequestDTO requestData, PatientTracker patientTracker) {
        patientTracker.setNationalId(enrolledPatient.getNationalId());
        patientTracker.setFirstName(enrolledPatient.getFirstName());
        patientTracker.setLastName(enrolledPatient.getLastName());
        patientTracker.setAge(enrolledPatient.getAge());
        patientTracker.setGender(enrolledPatient.getGender());
        patientTracker.setRegularSmoker(enrolledPatient.isRegularSmoker());
        patientTracker.setPhoneNumber(enrolledPatient.getPhoneNumber());
        patientTracker.setPatientId(enrolledPatient.getId());
        patientTracker.setPatientStatus(Constants.ENROLLED);
        patientTracker.setProgramId(enrolledPatient.getProgramId());
        patientTracker.setCountryId(enrolledPatient.getCountryId());
        patientTracker.setHeight(requestData.getBplog().getHeight());
        patientTracker.setWeight(requestData.getBplog().getHeight());
        patientTracker.setAvgSystolic(requestData.getBplog().getAvgSystolic());
        patientTracker.setAvgDiastolic(requestData.getBplog().getAvgDiastolic());
        patientTracker.setAvgPulse(requestData.getBplog().getAvgPulse());
        patientTracker.setBmi(requestData.getBplog().getBmi());
        patientTracker.setCvdRiskLevel(requestData.getCvdRiskLevel());
        patientTracker.setCvdRiskScore(requestData.getCvdRiskScore());
        patientTracker.setSiteId(enrolledPatient.getSiteId());
        patientTracker.setTenantId(enrolledPatient.getTenantId());
        patientTracker.setEnrollmentAt(new Date());
        patientTracker.setIsPregnant(enrolledPatient.getIsPregnant());
        patientTracker.setDateOfBirth(enrolledPatient.getDateOfBirth());
        patientTracker.setIsScreening((!Objects.isNull(patientTracker.getScreeningLogId())) ? true : false);
        if (!Objects.isNull(requestData.getGlucoseLog())) {
            patientTracker.setGlucoseValue(requestData.getGlucoseLog().getGlucoseValue());
            patientTracker.setGlucoseUnit(requestData.getGlucoseLog().getGlucoseUnit());
            patientTracker.setGlucoseType(requestData.getGlucoseLog().getGlucoseType());
        }


        // if (!Objects.isNull(requestData.getPhq4())) {
        //     MentalHealthDTO phq4 = requestData.getPhq4();
        //     patientTracker.setPhq4Score(phq4.getPhq4Score());
        //     patientTracker.setPhq4RiskLevel(phq4.getPhq4RiskLevel());
        //     patientTracker.setPhq4FirstScore(phq4.getPhq4FirstScore());
        //     patientTracker.setPhq4SecondScore(phq4.getPhq4SecondScore());
        // }
        if (!Objects.isNull(requestData.getProvisionalDiagnosis()) && !requestData.getProvisionalDiagnosis().isEmpty()) {
            patientTracker.setProvisionalDiagnosis(requestData.getProvisionalDiagnosis());

        }

        System.out.println("Provisional Diagnosis=====" + patientTracker.getProvisionalDiagnosis());
        return patientTracker;
    }

    /**
     * {@inheritDoc}
     */
    public PatientTrackerDTO getPatientDetails(PatientGetRequestDTO requestData) {
        ModelMapper modelMapper = new ModelMapper();
        PatientTracker patientTracker = patientTrackerService.getPatientTrackerById(requestData.getId());
        if (Objects.isNull(patientTracker)) {
            throw new DataNotFoundException(3004);
        }
//        TODO: find organizationUnit using data from params and unit conversion of that data.
        PatientTrackerDTO patientTrackerDTO = modelMapper.map(patientTracker, new TypeToken<PatientTrackerDTO>() {
        }.getType());
//        patientTrackerDTO.setPhq9(Constants.BOOLEAN_FALSE);
//        patientTrackerDTO.setGad7(Constants.BOOLEAN_FALSE);

        if (patientTrackerDTO.getPhq4Score() >= 2) {
            patientTrackerDTO.setGad7(Constants.BOOLEAN_TRUE);
        }
        if (patientTrackerDTO.getPhq4SecondScore() >= 2) {
            patientTrackerDTO.setPhq9(Constants.BOOLEAN_TRUE);
        }

        if (requestData.isAssessmentDataRequired()) {
            BpLog bpLog = bpLogService.getBpLogByPatientTrackIdAndIsLatest(requestData.getId(), Constants.BOOLEAN_TRUE);
            if (!Objects.isNull(bpLog)) {
                patientTrackerDTO.setBpLogDetails(bpLog.getBpLogDetails());
            GlucoseLog glucoseLog = glucoseLogService.getGlucoseLogByPatientTrackIdAndIsLatest(requestData.getId(),
                    Constants.BOOLEAN_TRUE);
            if (!Objects.isNull(glucoseLog)) {
                patientTrackerDTO.setGlucoseLogDetails(new GlucoseLogDTO(glucoseLog.getGlucoseType(),
                        glucoseLog.getGlucoseValue(), glucoseLog.getLastMealTime(), glucoseLog.getGlucoseDateTime(),
                        glucoseLog.getGlucoseUnit()));
            }
            ScreeningLog screeningLog = screeningLogService.getByIdAndIsLatest(patientTracker.getScreeningLogId());
            System.out.println("***************************screeningLog in patient details get" + screeningLog);
            if (!Objects.isNull(screeningLog)) {
                patientTrackerDTO.setPhoneNumberCategory(screeningLog.getPhoneNumberCategory());
            }
            }
        }
        if (requestData.isPrescriberRequired()) {
//            TODO :  add prescriber details
        }

        if (requestData.isLifeStyleRequired()) {
//            TODO : add lifstyle details
        }

        Date date = null;
        if (!Objects.isNull(patientTrackerDTO.getEnrollmentAt())) {
            date = patientTrackerDTO.getEnrollmentAt();
        } else {
            date = patientTracker.getCreatedAt();
        }
        
        patientTrackerDTO.setAge(CommonUtil.calculatePatientAge(patientTrackerDTO.getAge(), date));

        return patientTrackerDTO;
    }

    /**
     * {@inheritDoc}
     */
    public PatientPregnancyDetails createPregnancyDetails(PregnancyRequestDTO requestData) {
        isPatientTrackIdExist(requestData.getPatientTrackId());
        requestData.setTemperature(convertTemperatureUnitForPregnancy(requestData, UnitConstants.METRIC));
        PatientTracker patientTracker = patientTrackerService.getPatientTrackerById(requestData.getPatientTrackId());
        PatientPregnancyDetails pregnancyDetails = constructPregnancyData(requestData);
        pregnancyDetails.setPatientTrackId(patientTracker.getId());
        pregnancyDetails = pregnancyDetailsRepository.save(pregnancyDetails);
        updatePatientTrackForPregnancyDetails(patientTracker, requestData);
        pregnancyDetails.setTemperature(convertTemperatureUnitForPregnancy(requestData, UnitConstants.IMPERIAL));
        return pregnancyDetails;
    }

    /**
     * Constructs PatientPregnancyDetails Object.
     *
     * @param requestData Request data with pregnancy details.
     * @return Constructed pregnancy data.
     * @author Niraimathi S
     */
    private PatientPregnancyDetails constructPregnancyData(PregnancyRequestDTO requestData) {
        ModelMapper modelMapper = new ModelMapper();
        PatientPregnancyDetails pregnancyDetails = modelMapper.map(requestData,
                new TypeToken<PatientPregnancyDetails>() {
                }.getType());
        return pregnancyDetails;
    }

    /**
     * {@inheritDoc}
     */
    public PatientPregnancyDetails getPregnancyDetails(GetRequestDTO requestData) {
        isPatientTrackIdExist(requestData.getPatientTrackId());
        PatientPregnancyDetails pregnancyDetails = null;
        if (Objects.isNull(requestData.getPatientPregnancyId())) {
            pregnancyDetails = pregnancyDetailsRepository
                    .findByPatientTrackIdAndIsDeleted(requestData.getPatientTrackId(), Constants.BOOLEAN_FALSE);
//        TODO: find organizationUnit using data from params.

        } else {
            pregnancyDetails = pregnancyDetailsRepository.findByIdAndIsDeleted(requestData.getPatientPregnancyId(),
                    Constants.BOOLEAN_FALSE);
        }
        if (Objects.isNull(pregnancyDetails)) {
            throw new DataNotFoundException(12005);
        }

//        if (organizationUnit.unit_measurement === UnitConstants.IMPERIAL) {
        if (true) {
            pregnancyDetails.setTemperature(UnitConversion.convertTemperature(pregnancyDetails.getTemperature()
                    , UnitConstants.IMPERIAL));
        }
        return pregnancyDetails;
    }

    /**
     * Checks if PatientTrack id exists and throws exception if not.
     *
     * @param patientTrackId PatientTrackId
     * @author Niraimathi S
     */
    private void isPatientTrackIdExist(Long patientTrackId) {
        if (Objects.isNull(patientTrackId)) {
            throw new DataNotAcceptableException(10010);
        }
    }

    /**
     * {@inheritDoc}
     */
    public PatientPregnancyDetails updatePregnancyDetails(PregnancyRequestDTO requestData) {
        PatientPregnancyDetails pregnancyDetails = null;
        isPatientTrackIdExist(requestData.getPatientTrackId());
        requestData.setTemperature(convertTemperatureUnitForPregnancy(requestData, UnitConstants.METRIC));

        PatientPregnancyDetails existingPregnantDetails = pregnancyDetailsRepository
                .findByIdAndIsDeleted(requestData.getPatientPregnancyId(), Constants.BOOLEAN_FALSE);
        if (Objects.isNull(existingPregnantDetails)) {
            throw new DataNotFoundException(12005);
        }
        pregnancyDetails = constructPregnancyData(requestData);
        pregnancyDetails.setId(requestData.getPatientPregnancyId());
        boolean isDiagnosis = pregnancyDetails.getDiagnosis().stream()
                .anyMatch(data -> data.equalsIgnoreCase(Constants.NONE));
        if (isDiagnosis) {
            pregnancyDetails.setIsOnTreatment(existingPregnantDetails.getIsOnTreatment());
            pregnancyDetails.setDiagnosisTime(existingPregnantDetails.getDiagnosisTime());
        }
        pregnancyDetails = pregnancyDetailsRepository.save(pregnancyDetails);
        PatientTracker patientTracker = patientTrackerService.getPatientTrackerById(requestData.getPatientTrackId());
        updatePatientTrackForPregnancyDetails(patientTracker, requestData);

        pregnancyDetails.setTemperature(convertTemperatureUnitForPregnancy(requestData, UnitConstants.IMPERIAL));
        return pregnancyDetails;
    }

    /**
     * Updates the pregnancy details in patientTracker for pregnant patients.
     *
     * @param patientTracker patientTracker object to update.
     * @param requestData    Request data with pregnancy details
     * @author Niraimathi S
     */
    private void updatePatientTrackForPregnancyDetails(PatientTracker patientTracker, PregnancyRequestDTO requestData) {
        patientTracker.setIsPregnant(Constants.BOOLEAN_TRUE);
        patientTracker.setEstimatedDeliveryDate(requestData.getEstimatedDeliveryDate());
        patientTracker.setLastMenstrualPeriodDate(requestData.getLastMenstrualPeriodDate());
        patientTracker.setTenantId(requestData.getTenantId());
        patientTrackerService.addOrUpdatePatientTracker(patientTracker);
    }

    /**
     * Convert temperature unit from imperial to metric and vise versa.
     *
     * @param requestData Request data containing pregnancy details
     * @param unit        Unit to convert
     * @return converted temperature
     * @author Niraimathi S
     */
    private float convertTemperatureUnitForPregnancy(PregnancyRequestDTO requestData, String unit) {
        float temperature = requestData.getTemperature();
        if (requestData.getUnitMeasurement().equals(UnitConstants.IMPERIAL)) {
            temperature = UnitConversion.convertTemperature(requestData.getTemperature(), unit);
        }
        return temperature;
    }
}