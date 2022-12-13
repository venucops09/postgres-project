package com.mdtlabs.coreplatform.spiceservice.patientTracker.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;

@Repository
public interface PatientTrackerRepository extends JpaRepository<PatientTracker, Long> {

	public static final String UPDATE_PATIENTTRAKER_FOR_BP_LOG = "update PatientTracker as pTracker set pTracker.height=:height , pTracker.weight=:weight, pTracker.bmi=:bmi , pTracker.avgSystolic=:avgSystolic , pTracker.avgDiastolic=:avgDiastolic , pTracker.avgPulse=:avgPulse , pTracker.cvdRiskLevel=:cvdRiskLevel , pTracker.cvdRiskScore=:cvdRiskScore, pTracker.nextBpAssessmentDate=:nextBpAssessmentDate where pTracker.id=:patientTrackerId";

//     public static final String UPDATE_PATIENTTRAKER_FOR_GLUCOSE_LOG = "update PatientTracker as pTracker set pTracker.glucoseValue=:glucoseValue , pTracker.glucoseUnit=:glucoseUnit , pTracker.glucoseType=:glucoseType, pTracker.nextBgAssessmentDate=:nextBgAssessmentDate where pTracker.id=:patientTrackerId";

	public static final String UPDATE_RED_RISK_STATUS = "UPDATE PatientTracker as pTracker set pTracker.isRedRiskPatient=:status where pTracker.id=:patientTrackerId";

	public static final String GET_MY_PATIENTS_LIST = "SELECT * FROM patient_tracker"
			+ "  WHERE (CAST(:nextMedicalReviewStartDate AS Date) IS null OR next_medical_review_date >= CAST(:nextMedicalReviewStartDate AS Date))"
			+ "  AND (CAST(:nextMedicalReviewEndDate AS Date) IS null OR next_medical_review_date <= CAST(:nextMedicalReviewEndDate AS Date))"
			+ "  AND (CAST(:lastAssessmentStartDate AS Date) IS null OR next_bp_assesment_date >= CAST(:lastAssessmentStartDate AS Date))"
			+ "  AND (CAST(:lastAssessmentEndDate AS Date) IS null OR next_bp_assesment_date <= CAST(:lastAssessmentEndDate AS Date))"
			+ "  AND (CASE WHEN (:nextMedicalReviewDate = true) THEN (next_medical_review_date IS NOT null) ELSE true END) "
			+ "  AND (CASE WHEN (:lastAssessmentDate = true) THEN (next_bp_assesment_date IS NOT null) ELSE true END) "
			+ "  AND (:isRedRiskPatient IS null OR is_red_risk_patient = :isRedRiskPatient)"
			+ "  AND (:cvdRiskLevel IS null OR cvd_risk_level = :cvdRiskLevel)"
			+ "  AND ((:screeningReferral IS null OR screening_referral = :screeningReferral)"
			+ "  OR (:patientStatusNotScreened IS null OR patient_status != :patientStatusNotScreened) )"
			+ "  AND (:patientStatusEnrolled IS null OR patient_status = :patientStatusEnrolled)"
			+ "  AND (:patientStatusNotEnrolled IS null OR patient_status != :patientStatusNotEnrolled)";

	public static final String SEARCH_PATIENTS = "SELECT * FROM patient_tracker"
			+ " WHERE (:tenantId IS null OR tenant_id = :tenantId) "
			+ " AND (:operatingUnitId IS null OR operating_unit_id = :operatingUnitId)"
			+ " AND (CAST(:nextMedicalReviewStartDate AS Date) IS null OR next_medical_review_date >= CAST(:nextMedicalReviewStartDate AS Date))"
			+ " AND (CAST(:nextMedicalReviewEndDate AS Date) IS null OR next_medical_review_date <= CAST(:nextMedicalReviewEndDate AS Date))"
			+ " AND (CAST(:lastAssessmentStartDate AS Date) IS null OR next_bp_assesment_date >= CAST(:lastAssessmentStartDate AS Date))"
			+ " AND (CAST(:lastAssessmentEndDate AS Date) IS null OR next_bp_assesment_date <= CAST(:lastAssessmentEndDate AS Date))"
			+ " AND (CASE WHEN (:nextMedicalReviewDate = true) THEN (next_medical_review_date IS NOT null) ELSE true END) "
			+ " AND (CASE WHEN (:lastAssessmentDate = true) THEN (next_bp_assesment_date IS NOT null) ELSE true END) "
			+ " AND (:isRedRiskPatient IS null OR is_red_risk_patient = :isRedRiskPatient)"
			+ " AND (:cvdRiskLevel IS null OR cvd_risk_level = :cvdRiskLevel)"
			+ " AND ((:screeningReferral IS null OR screening_referral = :screeningReferral)"
			+ " AND (:isLabTestReferred IS null OR is_labtest_referred = :isLabTestReferred)"
			+ " AND (:isMedicationPrescribed IS null OR is_medication_prescribed = :isMedicationPrescribed)"
			+ " OR (:patientStatusNotScreened IS null OR patient_status != :patientStatusNotScreened) )"
			+ " AND (:patientStatusEnrolled IS null OR patient_status = :patientStatusEnrolled)"
			+ " AND (:patientStatusNotEnrolled IS null OR patient_status != :patientStatusNotEnrolled)  AND is_deleted = false"
			+ " AND ( CASE WHEN (:programId IS null) THEN (lower(national_id) = lower(:nationalId))"
			+ " ELSE (program_id = CAST(:programId AS BIGINT) OR national_id = :nationalId) END)";

	public static final String ADVANCE_SEARCH_PATIENTS = "SELECT * FROM patient_tracker"
			+ " WHERE (:firstName IS null OR lower(first_name) LIKE CONCAT(lower(:firstName), '%'))"
			+ " AND (:lastName IS null OR lower(last_name) LIKE CONCAT(lower(:lastName), '%'))"
			+ " AND (:phoneNumber IS null OR phone_number = :phoneNumber)"
			+ " AND (CAST(:nextMedicalReviewStartDate AS Date) IS null OR next_medical_review_date >= CAST(:nextMedicalReviewStartDate AS Date))"
			+ " AND (CAST(:nextMedicalReviewEndDate AS Date) IS null OR next_medical_review_date <= CAST(:nextMedicalReviewEndDate AS Date))"
			+ " AND (CAST(:lastAssessmentStartDate AS Date) IS null OR next_bp_assesment_date >= CAST(:lastAssessmentStartDate AS Date))"
			+ " AND (CAST(:lastAssessmentEndDate AS Date) IS null OR next_bp_assesment_date <= CAST(:lastAssessmentEndDate AS Date))"
			+ " AND (CASE WHEN (:nextMedicalReviewDate = true) THEN (next_medical_review_date IS NOT null) ELSE true END) "
			+ " AND (CASE WHEN (:lastAssessmentDate = true) THEN (next_bp_assesment_date IS NOT null) ELSE true END) "
			+ " AND (:isRedRiskPatient IS null OR is_red_risk_patient = :isRedRiskPatient)"
			+ " AND (:cvdRiskLevel IS null OR cvd_risk_level = :cvdRiskLevel)"
			+ " AND ((:screeningReferral IS null OR screening_referral = :screeningReferral)"
			+ " OR (:patientStatusNotScreened IS null OR patient_status != :patientStatusNotScreened) )"
			+ " AND (:patientStatusEnrolled IS null OR patient_status = :patientStatusEnrolled)"
			+ " AND (:patientStatusNotEnrolled IS null OR patient_status != :patientStatusNotEnrolled)"
			+ " AND (:isLabtestReferred IS null OR is_labtest_referred = :isLabtestReferred)"
			+ " AND (:isMedicationPrescribed IS null OR is_medication_prescribed = :isMedicationPrescribed)"
			+ " AND is_deleted = :isDeleted ";

	public static final String UPDATE_LATEST_REFERRAL = "UPDATE PatientTracker as tracker set "
			+ "tracker.isLabtestReferred = :isLabTestReferred, tracker.tenantId = :tenantId where tracker.id = :id ";

	public static final String UPDATE_MEDICATION_PRESCRIBED = "UPDATE PatientTracker as tracker set "
			+ "tracker.isMedicationPrescribed = :isMedicationPrescribed where tracker.id = :id ";

	public PatientTracker findByNationalId(String nationalId);

	@Query(value = GET_MY_PATIENTS_LIST, nativeQuery = true)
	public List<PatientTracker> getPatientsList(@Param("nextMedicalReviewStartDate") String medicalReviewStartDate,
			@Param("nextMedicalReviewEndDate") String medicalReviewEndDate,
			@Param("lastAssessmentStartDate") String assessmentStartDate,
			@Param("lastAssessmentEndDate") String assessmentEndDate,
			@Param("nextMedicalReviewDate") boolean nextMedicalReviewDate,
			@Param("lastAssessmentDate") boolean lastAssessmentDate,
			@Param("isRedRiskPatient") Boolean isRedRiskPatient, @Param("cvdRiskLevel") String cvdRiskLevel,
			@Param("screeningReferral") Boolean screeningReferral,
			@Param("patientStatusNotScreened") String patientStatusNotScreened,
			@Param("patientStatusEnrolled") String patientStatusEnrolled,
			@Param("patientStatusNotEnrolled") String patientStatusNotEnrolled);

	@Query(value = GET_MY_PATIENTS_LIST, nativeQuery = true)
	public Page<PatientTracker> getPatientsListWithPagination(
			@Param("nextMedicalReviewStartDate") String medicalReviewStartDate,
			@Param("nextMedicalReviewEndDate") String medicalReviewEndDate,
			@Param("lastAssessmentStartDate") String assessmentStartDate,
			@Param("lastAssessmentEndDate") String assessmentEndDate,
			@Param("nextMedicalReviewDate") boolean nextMedicalReviewDate,
			@Param("lastAssessmentDate") boolean lastAssessmentDate,
			@Param("isRedRiskPatient") Boolean isRedRiskPatient, @Param("cvdRiskLevel") String cvdRiskLevel,
			@Param("screeningReferral") Boolean screeningReferral,
			@Param("patientStatusNotScreened") String patientStatusNotScreened,
			@Param("patientStatusEnrolled") String patientStatusEnrolled,
			@Param("patientStatusNotEnrolled") String patientStatusNotEnrolled, Pageable pageable);

	@Query(value = GET_MY_PATIENTS_LIST, nativeQuery = true)
	public List<PatientTracker> getPatientsList(@Param("nextMedicalReviewStartDate") String medicalReviewStartDate,
			@Param("nextMedicalReviewEndDate") String medicalReviewEndDate,
			@Param("lastAssessmentStartDate") String assessmentStartDate,
			@Param("lastAssessmentEndDate") String assessmentEndDate,
			@Param("isRedRiskPatient") Boolean isRedRiskPatient, @Param("cvdRiskLevel") String cvdRiskLevel,
			@Param("screeningReferral") Boolean screeningReferral,
			@Param("patientStatusNotScreened") String patientStatusNotScreened,
			@Param("patientStatusEnrolled") String patientStatusEnrolled,
			@Param("patientStatusNotEnrolled") String patientStatusNotEnrolled);

	@Query(value = GET_MY_PATIENTS_LIST, nativeQuery = true)
	public Page<PatientTracker> getPatientsListWithPagination(
			@Param("nextMedicalReviewStartDate") String medicalReviewStartDate,
			@Param("nextMedicalReviewEndDate") String medicalReviewEndDate,
			@Param("lastAssessmentStartDate") String assessmentStartDate,
			@Param("lastAssessmentEndDate") String assessmentEndDate,
			@Param("isRedRiskPatient") Boolean isRedRiskPatient, @Param("cvdRiskLevel") String cvdRiskLevel,
			@Param("screeningReferral") Boolean screeningReferral,
			@Param("patientStatusNotScreened") String patientStatusNotScreened,
			@Param("patientStatusEnrolled") String patientStatusEnrolled,
			@Param("patientStatusNotEnrolled") String patientStatusNotEnrolled, Pageable pageable);

	public PatientTracker findByIdAndIsDeleted(long id, boolean isDeleted);

	@Query(value = SEARCH_PATIENTS, nativeQuery = true)
	public Page<PatientTracker> searchPatientsWithPagination(@Param("tenantId") Long tenantId,
			@Param("operatingUnitId") Long operatingUnitId,
			@Param("nextMedicalReviewStartDate") String medicalReviewStartDate,
			@Param("nextMedicalReviewEndDate") String medicalReviewEndDate,
			@Param("lastAssessmentStartDate") String assessmentStartDate,
			@Param("lastAssessmentEndDate") String assessmentEndDate,
			@Param("nextMedicalReviewDate") boolean nextMedicalReviewDate,
			@Param("lastAssessmentDate") boolean lastAssessmentDate,
			@Param("isRedRiskPatient") Boolean isRedRiskPatient, @Param("cvdRiskLevel") String cvdRiskLevel,
			@Param("screeningReferral") Boolean screeningReferral,
			@Param("isLabTestReferred") Boolean isLabTestReferred,
			@Param("isMedicationPrescribed") Boolean isMedicationPrescribed,
			@Param("patientStatusNotScreened") String patientStatusNotScreened,
			@Param("patientStatusEnrolled") String patientStatusEnrolled,
			@Param("patientStatusNotEnrolled") String patientStatusNotEnrolled, @Param("nationalId") String nationalId,
			@Param("programId") String programId, Pageable pageable);

	@Query(value = ADVANCE_SEARCH_PATIENTS, nativeQuery = true)
	public Page<PatientTracker> getPatientsWithAdvanceSearch(@Param("firstName") String firstName,
			@Param("lastName") String lastName, @Param("phoneNumber") String phoneNumber,
			@Param("nextMedicalReviewStartDate") String medicalReviewStartDate,
			@Param("nextMedicalReviewEndDate") String medicalReviewEndDate,
			@Param("lastAssessmentStartDate") String assessmentStartDate,
			@Param("lastAssessmentEndDate") String assessmentEndDate,
			@Param("nextMedicalReviewDate") boolean nextMedicalReviewDate,
			@Param("lastAssessmentDate") boolean lastAssessmentDate,
			@Param("isRedRiskPatient") Boolean isRedRiskPatient, @Param("cvdRiskLevel") String cvdRiskLevel,
			@Param("screeningReferral") Boolean screeningReferral,
			@Param("patientStatusNotScreened") String patientStatusNotScreened,
			@Param("patientStatusEnrolled") String patientStatusEnrolled,
			@Param("patientStatusNotEnrolled") String patientStatusNotEnrolled,
			@Param("isLabtestReferred") Boolean isLabtestReferred,
			@Param("isMedicationPrescribed") Boolean isMedicationPrescribed, @Param("isDeleted") boolean isDeleted,
			Pageable pageable);
//
//	@Query(value = SEARCH_PATIENTS, nativeQuery = true)
//	public Page<PatientTracker> searchPatientsWithPagination(
//			@Param("nextMedicalReviewStartDate") String medicalReviewStartDate,
//			@Param("nextMedicalReviewEndDate") String medicalReviewEndDate,
//			@Param("lastAssessmentStartDate") String assessmentStartDate,
//			@Param("lastAssessmentEndDate") String assessmentEndDate,
//			@Param("isRedRiskPatient") Boolean isRedRiskPatient, @Param("cvdRiskLevel") String cvdRiskLevel,
//			@Param("screeningReferral") Boolean screeningReferral,
//			@Param("patientStatusNotScreened") String patientStatusNotScreened,
//			@Param("patientStatusEnrolled") String patientStatusEnrolled,
//			@Param("patientStatusNotEnrolled") String patientStatusNotEnrolled, @Param("nationalId") String nationalId,
//			@Param("programId") String programId, Pageable pageable);

	/**
	 * Updates fields like height, weight, etc., for BPLog
	 *
	 * @param height           patient height
	 * @param weight           patient weight
	 * @param bmi              patient BMI
	 * @param avgSystolic      Average systolic value
	 * @param avgDiastolic     Average diastolic value
	 * @param avgPulse         Average pulse
	 * @param cvdRiskLevel     cvd risklevel of a patient
	 * @param cvdRiskScore     cvd risk score
	 * @param patientTrackerId patientTrackId
	 * @return updated no. of rows
	 */
//     @Transactional
//     @Modifying
//     @Query(value = UPDATE_PATIENTTRAKER_FOR_BP_LOG)
//     public int updatePatientTrackerForBpLog(
//             @Param("height") float height,
//             @Param("weight") float weight,
//             @Param("bmi") float bmi,
//             @Param("avgSystolic") int avgSystolic,
//             @Param("avgDiastolic") int avgDiastolic,
//             @Param("avgPulse") int avgPulse,
//             @Param("cvdRiskLevel") String cvdRiskLevel,
//             @Param("cvdRiskScore") Integer cvdRiskScore,
//             @Param("nextBpAssessmentDate") Date nextBpAssessmentDate,
//             @Param("patientTrackerId") long patientTrackerId);

	/**
	 * Updates patientTracker fields for glucose log.
	 *
	 * @param glucoseValue     patient glucose value
	 * @param glucoseUnit      patient glucose unit
	 * @param glucoseType      glucose type
	 * @param patientTrackerId patient track Id.
	 * @return No. of updated rows
	 */
//     @Transactional
//     @Modifying
//     @Query(value = UPDATE_PATIENTTRAKER_FOR_GLUCOSE_LOG)
//     public int updatePatientTrackerForGlucoseLog(
//             @Param("glucoseValue") int glucoseValue,
//             @Param("glucoseUnit") String glucoseUnit,
//             @Param("glucoseType") String glucoseType,
//             @Param("nextBgAssessmentDate") Date nextBgAssessmentDate,
//             @Param("patientTrackerId") long patientTrackerId);

	/**
	 * This method finds a patientTracker entity by national Id.
	 *
	 * @param nationalId
	 * @return PatientTracker Entity
	 */
	public PatientTracker findByNationalIdIgnoreCase(String nationalId);

	@Transactional
	@Modifying
	@Query(value = UPDATE_RED_RISK_STATUS)
	public int updateRedRiskPatientStatus(@Param("patientTrackerId") long patientTrackerId,
			@Param("status") boolean status);

	/**
	 * Updates isLabTestReferred and tenantId for a patientTrackerId.
	 *
	 * @param patientTrackId
	 * @param tenantId
	 * @param isLabTestReferred
	 * @author Niraimathi S
	 */
	@Transactional
	@Modifying
	@Query(value = UPDATE_LATEST_REFERRAL)
	void updatePatientTrackerLabtestReferral(@Param("id") long patientTrackId, @Param("tenantId") Long tenantId,
			@Param("isLabTestReferred") boolean isLabTestReferred);

	@Transactional
	@Modifying
	@Query(value = UPDATE_MEDICATION_PRESCRIBED)
	void updateForFillPrescription(@Param("id") long id,
			@Param("isMedicationPrescribed") boolean isMedicationPrescribed);

}
