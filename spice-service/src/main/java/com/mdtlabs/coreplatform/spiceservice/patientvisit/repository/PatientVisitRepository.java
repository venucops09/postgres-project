package com.mdtlabs.coreplatform.spiceservice.patientvisit.repository;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientVisit;

@Repository
public interface PatientVisitRepository extends JpaRepository<PatientVisit, Long> {

	// public static final String GET_PATIENT_VISIT_BY_TRACK_ID =
	// "From PatientVisit as patientvisit where
	// patientvisit.patientTrackId=:patientTrackId and patientvisit.isDeleted=false
	// and patientvisit.visitDate between CAST(:startDate AS Date) and CAST(:endDate
	// AS Date)";

	public static final String GET_PATIENT_VISIT_BY_TRACK_ID = "SELECT * from patient_visit where patient_track_id=:patientTrackId and is_deleted=false and visit_date between CAST(:startDate AS TIMESTAMP) and CAST(:endDate AS TIMESTAMP)";

	public static final String GET_VISIT_DATES = "select visit.visitDate from PatientVisit as visit where (:isMedicalReview is null or visit.isMedicalReview=:isMedicalReview)"
			+ "and (:isInvestigation is null or visit.isInvestigation=:isInvestigation) and (:isPrescription is null or visit.isPrescription=:isPrescription)"
			+ "and visit.patientTrackId=:patientTrackId ORDER BY visit.visitDate";

	public static final String GET_PATIENT_VISIT_DATES = "SELECT visit from PatientVisit as visit where "
			+ "visit.patientTrackId =:patientTrackId AND (:isInvestigation is null or visit.isInvestigation"
			+ "=:isInvestigation) AND (:isMedicalReview is null or visit.isMedicalReview=:isMedicalReview) "
			+ "AND (:isPrescription is null or visit.isPrescription=:isPrescription) AND visit.isDeleted=false "
			+ "ORDER BY visit.visitDate";


	// public static final String GET_PATIENT_VISIT_BY_TRACK_ID =
	// "From PatientVisit as patientvisit where
	// patientvisit.patientTrackId=:patientTrackId and patientvisit.isDeleted=false
	// and patientvisit.visitDate between CAST(:startDate AS Date) and CAST(:endDate
	// AS Date)";

	// public static final String GET_PATIENT_VISIT_BY_TRACK_ID =
	// "SELECT * from patient_visit where patient_track_id=:patientTrackId and
	// is_deleted=false and visit_date between :startDate and :endDate";

	// public static final String UPDATE_FOR_PATIENT_LABTEST = " UPDATE PatientVisit
	// visit set visit.isInvestigation "
	// + "= :isInvestigation, visit.tenantId=:tenantId where visit.id=:visitId";

	@Query(value = GET_PATIENT_VISIT_BY_TRACK_ID, nativeQuery = true)
	public PatientVisit getPatientVisitByTrackId(@Param("patientTrackId") Long patientTrackId,
			@Param("startDate") String startDate, @Param("endDate") String endDate);

	@Query(value = GET_VISIT_DATES)
	public List<Date> getVisitDates(@Param("isMedicalReview") Boolean isMedicalReview,
			@Param("isInvestigation") Boolean isInvestigation, @Param("isPrescription") Boolean isPrescription,
			@Param("patientTrackId") Long patientTrackId);

	/**
	 * Gets patientVisit dates based on fields like patientTrackId, isInvestigation,
	 * isMedicalReview and isPrescription.
	 *
	 * @param patientTrackId  Patient tracker Id
	 * @param isInvestigation
	 * @param isMedicalReview
	 * @param isPrescription
	 * @return List of PatientVisit Entities.
	 * @author Niraimathi S
	 */
	@Query(value = GET_PATIENT_VISIT_DATES)
	List<PatientVisit> getPatientVisitDates(@Param("patientTrackId") Long patientTrackId,
			@Param("isInvestigation") Boolean isInvestigation, @Param("isMedicalReview") Boolean isMedicalReview,
			@Param("isPrescription") Boolean isPrescription);


    public PatientVisit findByIdAndIsDeleted(Long id, boolean isDeleted);

    public PatientVisit findByIdAndTenantId(Long id, Long tenantId);

}
