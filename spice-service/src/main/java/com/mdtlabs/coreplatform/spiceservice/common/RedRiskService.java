package com.mdtlabs.coreplatform.spiceservice.common;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.model.dto.spice.RedRiskDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientComorbidity;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientDiagnosis;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.common.model.entity.spice.RedRiskNotification;
import com.mdtlabs.coreplatform.spiceservice.common.repository.PatientComorbidityRepository;
import com.mdtlabs.coreplatform.spiceservice.common.repository.PatientDiagnosisRepository;
import com.mdtlabs.coreplatform.spiceservice.common.repository.RedRiskNotificationRepository;


@Service
public class RedRiskService {

    private static PatientDiagnosisRepository patientDiagnosisRepository;
   
    private static PatientComorbidityRepository patientComorbidityRepository;
 
    private static RedRiskNotificationRepository redRiskNotificationRepository;


    @Autowired
    private RedRiskService(PatientDiagnosisRepository patientDiagnosisRepository, 
                    PatientComorbidityRepository patientComorbidityRepository, 
                    RedRiskNotificationRepository redRiskNotificationRepository ) {
        RedRiskService.patientDiagnosisRepository = patientDiagnosisRepository;
        RedRiskService.patientComorbidityRepository = patientComorbidityRepository;
        RedRiskService.redRiskNotificationRepository = redRiskNotificationRepository;
    }

    public static String convertRiskLevelToBPRiskLevel(String riskLevel) {
        // Convert risk level to bp risk level: ${riskLevel}
        if (riskLevel == Constants.HIGHER_MODERATE || riskLevel == Constants.GLUCOSE_MODERATE
                || riskLevel == Constants.BOTH_MODERATE || riskLevel == Constants.BOTH_HIGHER_MODERATE) {

            return Constants.MODERATE;
        }

        return riskLevel;
    }

    public static String calculateRiskMessage(String riskLevel) {
        // Calculates patient risk message. risk level: ${riskLevel}
        String riskMessage = "";
        switch (riskLevel) {
            case Constants.GLUCOSE_MODERATE:
                riskMessage = Constants.DBM_GLUCOSE_MODERATE;
                break;
            case Constants.BOTH_MODERATE:
                riskMessage = Constants.DBM_BOTH_MODERATE;
                break;
            case Constants.BOTH_HIGHER_MODERATE:
                riskMessage = Constants.DBM_BOTH_MODERATE;
                break;
            case Constants.HIGHER_MODERATE:
                riskMessage = Constants.DBM_HIGHER_MODERATE;
                break;
            case Constants.HIGH:
                riskMessage = Constants.DBM_HIGH;
                break;
            case Constants.LOW:
                riskMessage = Constants.DBM_LOW;
                break;
            default:
                riskMessage = Constants.DBM_MODERATE;
        }
        return riskMessage;
    }

    /**
     * Get patient risk level only if patient enrolled and completed the initial
     * medical review
     * 
     * @param patientTracker
     * @param redRiskDTO
     * @return redRisk
     * @author Niraimathi S
     */
    public static String getPatientRiskLevel(PatientTracker patientTracker, RedRiskDTO redRiskDTO) {

        System.out.println("================getPatientRiskLevel");
        System.out.println(!Objects.isNull(redRiskDTO.getDiabetesPatientType()));
        System.out.println(!redRiskDTO.getDiabetesPatientType().equals(Constants.KNOWN_DIABETES_PATIENT));
        
        if (!Objects.isNull(redRiskDTO.getDiabetesPatientType())
                && !redRiskDTO.getDiabetesPatientType().equals(Constants.KNOWN_DIABETES_PATIENT)) {

                    System.out.println("=====================geting patientDiagnosis");
                    System.out.println(patientTracker.getId());
            PatientDiagnosis patientDiagnosis = patientDiagnosisRepository.findByPatientTrackIdAndIsActiveAndIsDeleted(
                    patientTracker.getId(), true, false);

            System.out.println("+===================================patientDiagnosis: " + patientDiagnosis);
            List<PatientComorbidity> patientComorbidities = patientComorbidityRepository.getBytrackerId(
                    patientTracker.getId(),
                    Constants.BOOLEAN_TRUE, Constants.BOOLEAN_FALSE);
            System.out.println("+===================================patientComorbitity :" + patientComorbidities);
            redRiskDTO.setDiabetesDiagControlledType(!Objects.isNull(patientDiagnosis)
                    && !Objects.isNull(patientDiagnosis.getDiabetesDiagControlledType())
                            ? patientDiagnosis.getDiabetesDiagControlledType()
                            : "");
            redRiskDTO.setComorbiditiesCount(patientComorbidities.size());

        }
        return RiskAlgorithm.getRiskLevelForNewPatient(patientTracker, redRiskDTO);
    }

	public static RedRiskNotification createRedRiskNotification(RedRiskNotification redRiskNotification) {
      // Create red risk notification. patient tracker ID: ${notificationData.patient_track_id}
      return redRiskNotificationRepository.save(redRiskNotification);
      
    }

    public static void updateRedRiskNoticationStatus(Long patientTrackId) {
        redRiskNotificationRepository.updateRedRiskStatus(Constants.MEDICAL_REVIEW_COMPLETED, patientTrackId, Constants.NEW);
    }


    /*onst updateRedRiskNoticationStatus = async (app, params, patientTrackerId) => {
  try {
    log.info(`Update the red risk notification status to completed. patient tracker ID: ${patientTrackerId}`);
    const redriskUpdateData = {
      status: AppConstants.REDRISK_NOTIFICATION_STATUS.MEDICAL_REVIEW_COMPLETED
    };
    const copyParams = core_util.cloneParams(params);
    copyParams.query = {
      patient_track_id: patientTrackerId,
      is_deleted: false,
      status: AppConstants.REDRISK_NOTIFICATION_STATUS.NEW,
      is_multi_update: true,
      tenant_id: params.query.tenant_id
    };
    copyParams.route.modelName = AppConstants.MODEL.RED_RISK_NOTIFICATION;
    await app.service('forms/:modelName').patch(null, redriskUpdateData, copyParams);
  } catch (error) {
    log.error(`Error while update the red risk notfication status - ${error}`);
    throw error;
  } */
}
