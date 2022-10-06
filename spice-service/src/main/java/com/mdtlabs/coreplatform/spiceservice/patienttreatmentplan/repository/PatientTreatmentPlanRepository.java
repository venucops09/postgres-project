package com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTreatmentPlan;


@Repository
public interface PatientTreatmentPlanRepository extends JpaRepository<PatientTreatmentPlan, Long> {

	// public static final String GET_PATIENT_TREATEMENT_PLAN = 
	// "from PatientTreatmentPlan as treatmentplan where treatmentplan.patientTrackId=:patientTrackId and treatmentplan.isDeleted=false and "+
	// "treatmentplan.tenantId=:tenantId order by treatmentplan.modifiedAt DESC limit 1";

	public static final String GET_PATIENT_TREATEMENT_PLAN = 
	"Select * from patienttreatmentplan where patient_track_id=:patientTrackId and " +
	"is_deleted=false and tenant_id=:tenantId order by modified_at DESC limit 1";

	List<PatientTreatmentPlan> findByPatientTrackIdAndIsDeletedOrderByUpdatedAtDesc(long patientTrackId, boolean isDeleted);

//	PatientTreatmentPlan findByPatientTrackIdAndRiskLevelIgnoreCaseAndTenantId(Long id, String cvdRiskLevel,
//			long tenantId);
	@Query(value = GET_PATIENT_TREATEMENT_PLAN, nativeQuery = true)
	PatientTreatmentPlan getTreatementPlanDetails(@Param("patientTrackId")Long patientTrackId, @Param("tenantId")Long tenantId);


	PatientTreatmentPlan findByPatientTrackIdAndTenantId(Long id, Long tenantId);

	PatientTreatmentPlan findByIdAndIsDeleted(long id, boolean isDeleted);

}
