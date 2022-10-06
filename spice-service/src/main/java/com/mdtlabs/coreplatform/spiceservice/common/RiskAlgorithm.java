package com.mdtlabs.coreplatform.spiceservice.common;

import java.util.Objects;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.model.dto.spice.RedRiskDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RiskAlgorithmDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;


public class RiskAlgorithm {

    public static String getRiskLevelInAssessmentDBM(RiskAlgorithmDTO riskAlgorithmDTO) {
        String riskLevel = null;
        if (!Objects.isNull(riskAlgorithmDTO.getIsPregnant()) && riskAlgorithmDTO.getIsPregnant()) {
          // Patient preganancy status is true so patient risk level is high
          riskLevel = Constants.HIGH;
    
        } else if (!Objects.isNull(riskAlgorithmDTO.getRiskLevel())) {
          riskAlgorithmDTO.setRiskLevel(calculateRiskLevelFromBPReading(riskAlgorithmDTO.getAvgSystolic(),
            riskAlgorithmDTO.getAvgDiastolic(), riskAlgorithmDTO.getRiskLevel()));
    
          riskLevel = calculateRiskFromSymptomAndBPReading(riskAlgorithmDTO);
        }
        return riskLevel;
      }

    public static String calculateRiskLevelFromBPReading(int avgSystolic, int avgDiastolic, String existingRiskLevel) {
        // Calculate the patient risk level based on patient BP and existing risk level
        int systolicThreshold = Constants.BP_THRESHOLD_SYSTOLIC;
        int diastolicThreshold = Constants.BP_THRESHOLD_DIASTOLIC;
        String riskLevel = null;

        if (avgSystolic <= systolicThreshold && avgDiastolic <= diastolicThreshold) {
            riskLevel = Constants.LOW;
        }

        if (((avgSystolic >= 141 && avgSystolic <= 160) || (avgDiastolic >= 91 && avgDiastolic <= 99)) &&
                existingRiskLevel == Constants.HIGH) {
            riskLevel = Constants.MODERATE;
        }

        if ((avgSystolic > systolicThreshold && avgSystolic <= 179) ||
                (avgDiastolic > diastolicThreshold && avgDiastolic <= 109 &&
                        (existingRiskLevel == Constants.MODERATE || existingRiskLevel == Constants.LOW))) {
            riskLevel = Constants.MODERATE;
        }

        if (((avgSystolic >= 161 && avgSystolic <= 179) ||
                (avgDiastolic >= 100 && avgDiastolic <= 109)) &&
                existingRiskLevel == Constants.HIGH) {
            riskLevel = Constants.HIGHER_MODERATE;
        }

        if ((avgSystolic >= 180 && avgSystolic <= 199) ||
                (avgDiastolic >= 110 && avgDiastolic <= 119)) {
            riskLevel = Constants.HIGHER_MODERATE;
        }
        return riskLevel;
    }

    public static String calculateRiskFromSymptomAndBPReading(RiskAlgorithmDTO riskAlgorithmDTO) {
        // Calculate the patient risk level based on patient BP, glucose and symptoms
        // data
        // Set<SymptomDTO> symptomDtos = (Set<SymptomDTO>) patientData.get("symptoms");
    
        // if (!symptomDtos.isEmpty()) {
          
        // }
    
        // TODO: symtom
        if (riskAlgorithmDTO.getAvgSystolic() >= 200 || riskAlgorithmDTO.getAvgSystolic() >= 120) {
          riskAlgorithmDTO.setRiskLevel(Constants.HIGH);
        }
        if (null != riskAlgorithmDTO.getGlucoseValue()) {
          // Calculate the patient risk level using patient glucose values
          riskAlgorithmDTO.setRiskLevel(calculateRiskLevelFromGlucoseData(riskAlgorithmDTO.getGlucoseValue(),
              riskAlgorithmDTO.getGlucoseType(), riskAlgorithmDTO.getRiskLevel()));
        }
        return riskAlgorithmDTO.getRiskLevel();
    
      }

    public static String calculateRiskLevelFromGlucoseData(Float glucoseValue, String glucoseType,
            String existingRiskLevel) {

        if (existingRiskLevel != Constants.HIGH &&
                ((glucoseType == Constants.RBS &&
                        (glucoseValue >= 13 || glucoseValue < 4)) ||
                        (glucoseType == Constants.FBS &&
                                (glucoseValue > 11 || glucoseValue < 4))
                        ||
                        (glucoseType == Constants.RBS &&
                                (glucoseValue > 11 || glucoseValue < 4)))) {
            existingRiskLevel = Constants.HIGH;
        }
        if ((glucoseType == Constants.RBS && glucoseValue > 10 &&
                glucoseValue < 12.9)
                || (glucoseType == Constants.FBS &&
                        glucoseValue > 7.8 && glucoseValue < 11)
                ||
                (glucoseType == Constants.RBS &&
                        glucoseValue > 7.8 &&
                        glucoseValue < 11)) {
            switch (existingRiskLevel) {

                case Constants.LOW:
                    existingRiskLevel = Constants.GLUCOSE_MODERATE;
                    break;
                case Constants.MODERATE:
                    existingRiskLevel = Constants.BOTH_MODERATE;
                    break;
                case Constants.HIGHER_MODERATE:
                    existingRiskLevel = Constants.BOTH_HIGHER_MODERATE;
                    break;
            }
        }
        return existingRiskLevel;
    }

    /**
     * Get risk level for new patient based on patient comorbidities, risk factors,
     * diabetes diagnosis
     * and average BP
     * 
     * @param patientTracker PatientTracker object
     * @param redRiskDTO     RedRiskDTO object
     * @return riskLevel of the new Patient.
     * @author Niraimathi S
     */
    public static String getRiskLevelForNewPatient(PatientTracker patientTracker, RedRiskDTO redRiskDTO) {
        String riskLevel = null;
        if (patientTracker.getIsPregnant() == Constants.BOOLEAN_TRUE) {
            riskLevel = Constants.HIGH;
        } else if (1 <= redRiskDTO.getComorbiditiesCount() || redRiskDTO.getDiabetesDiagControlledType()
                .equals(Constants.DIABETES_UNCONTROLLED_OR_POORLY_CONTROLLED)) {
            riskLevel = Constants.HIGH;
        } else {
            int riskFactorsCount = getRiskFactorsCount(patientTracker);
            riskLevel = calculateRiskFromBPAndRiskFactors(patientTracker, riskFactorsCount, redRiskDTO);
        }
        System.out.println("-=-=-=-=-=---------------------------riskLevel in getRiskLevelForNewPatient" + riskLevel);
        return riskLevel;
    }

    /**
     * Get patient Risk factor count.
     * 
     * @param patientTracker PatientTracker object
     * @return riskFactorCount risk factor count
     * @author Niraimathi S
     */
    public static int getRiskFactorsCount(PatientTracker patientTracker) {
        int riskFactorCount = 0;
        if (patientTracker.getGender().equals(Constants.GENDER_MALE)) {
            riskFactorCount++;
        }

        if ((patientTracker.getGender().equals(Constants.GENDER_MALE) && patientTracker.getAge() >= 55)
                || (patientTracker.getGender().equals(Constants.GENDER_FEMALE) && patientTracker.getAge() >= 65)) {
            riskFactorCount++;
        }

        if (patientTracker.isRegularSmoker() == Constants.BOOLEAN_TRUE) {
            riskFactorCount++;
        }

        if (patientTracker.getGlucoseType() == Constants.FBS &&
                patientTracker.getGlucoseValue() >= 5.6 &&
                patientTracker.getGlucoseValue() <= 6.9) {
            riskFactorCount++;
        }
        if (patientTracker.getBmi() >= 30) {
            riskFactorCount++;
        }
        System.out.println("-------------------------riskFactorCount" + riskFactorCount);
        return riskFactorCount;
    }

    /**
     * Calculate the patient risk level from patient BP level, risk factors count
     * and diabetes diagnosis status
     * 
     * @param patientTracker   PatientTracker object
     * @param riskFactorsCount riskFactor count
     * @param redRiskDTO       ResRiskDTO object
     * @return risk level of the patient.
     * @author Niraimathi S
     */
    private static String calculateRiskFromBPAndRiskFactors(PatientTracker patientTracker, int riskFactorsCount,
            RedRiskDTO redRiskDTO) {
        String riskLevel = null;
        if (patientTracker.getAvgSystolic() >= 180 || patientTracker.getAvgDiastolic() >= 110) {
            riskLevel = Constants.HIGH;
        } else if ((patientTracker.getAvgSystolic() >= 160 && patientTracker.getAvgSystolic() <= 179) ||
                (patientTracker.getAvgDiastolic() >= 100 && patientTracker.getAvgDiastolic() <= 109)) {
            if (riskFactorsCount > 0 ||
                    redRiskDTO.getDiabetesDiagControlledType() == Constants.DIABETES_WELL_CONTROLLED ||
                    redRiskDTO.getDiabetesDiagControlledType() == Constants.PRE_DIABETES) {
                riskLevel = Constants.HIGH;
            } else {
                riskLevel = Constants.MODERATE;
            }
        } else if ((patientTracker.getAvgSystolic() >= 140 && patientTracker.getAvgSystolic() <= 159) ||
                (patientTracker.getAvgDiastolic() >= 90 && patientTracker.getAvgDiastolic() <= 99)) {
            if (riskFactorsCount >= 3 ||
                    redRiskDTO.getDiabetesDiagControlledType() == Constants.DIABETES_WELL_CONTROLLED) {
                riskLevel = Constants.HIGH;
            } else if ((riskFactorsCount > 0 && riskFactorsCount <= 2) ||
                    redRiskDTO.getDiabetesDiagControlledType() == Constants.PRE_DIABETES) {
                riskLevel = Constants.MODERATE;
            } else {
                riskLevel = Constants.LOW;
            }
        } else if (patientTracker.getAvgSystolic() <= 139 || patientTracker.getAvgSystolic() <= 89) {
            if (riskFactorsCount >= 3 ||
                    redRiskDTO.getDiabetesDiagControlledType() == Constants.DIABETES_WELL_CONTROLLED) {
                riskLevel = Constants.MODERATE;
            } else {
                riskLevel = Constants.LOW;
            }
        }
        System.out.println("------------------------------riskLevel in calculateRiskFromBPAndRiskFactors" + riskLevel);
        return riskLevel;
    }
}