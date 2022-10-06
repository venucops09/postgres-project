package com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTreatmentPlan;


public interface PatientTreatmentPlanService {

    /**
     * Adds a new patient treatment plan.
     * 
     * @param treatmentPlan
     * @return
     * @author Niraimathi S
     */
    public PatientTreatmentPlan addPatientTreatmentPlan(PatientTreatmentPlan treatmentPlan);

    /**
     * Gets a patient treatment plan based on patient track id.
     * 
     * @param patientTrackId
     * @return
     * @author Niraimathi S
     */
    public PatientTreatmentPlan getPatientTreatmentPlan(Long patientTrackId);

    /**
     * 
     * @param id
     * @param tenantId
     * @return
     * @author Niraimathi S
     */
    public PatientTreatmentPlan getPatientTreatmentPlanDetails(Long id, Long tenantId);

    /**
     * 
     * @param id
     * @param cvdRiskLevel
     * @param tenantId
     * @return
     * @author Niraimathi S
     */
    public PatientTreatmentPlan getPatientTreatmentPlanDetails(Long id, String cvdRiskLevel, Long tenantId);


    /**
     * Creates a psovisional treatment plan for a patient.
     * 
     * @param patientTracker
     * @param cvdRiskLevel
     * @param tenantId
     * @return treatment plan response
     * @author Niraimathi S
     */
    public List<Map<String, String>> createProvisionalTreatmentPlan(PatientTracker patientTracker, String cvdRiskLevel,
            Long tenantId);

    public Date getTreatmentPlanFollowupDate(String frequencyName, String frequencyType);

    /**
     * Updates the ptient treatment paln data
     * 
     * @param patientTreatmentPlan
     * @return boolean
     * @author Karthick Muruegesan
     */
    public boolean updateTreatmentPlanData(PatientTreatmentPlan patientTreatmentPlan);

}
    