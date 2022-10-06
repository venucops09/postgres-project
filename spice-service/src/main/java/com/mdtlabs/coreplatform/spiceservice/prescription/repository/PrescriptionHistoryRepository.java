package com.mdtlabs.coreplatform.spiceservice.prescription.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PrescriptionHistory;


@Repository
public interface PrescriptionHistoryRepository extends JpaRepository<PrescriptionHistory, Long> {

	public static final String GET_PRESCRIPTIONS = 
	"select * from (" +
		"select *, rank() over (partition by precription_id order by created_at desc) from prescription_history where patient_visit_id=:patientVisitId" +
		") where rank = 1";


	public static final String GET_PRESCRIPTION_HISTORY = "SELECT * FROM (SELECT *, row_number() OVER (PARTITION BY prescription_id,"
			+ " patient_visit_id ORDER BY created_at DESC) AS row_number FROM prescription_history "
			+ " WHERE (:prescriptionId IS null OR prescription_id = :prescriptionId )"
			+ " AND (:patientVisitId IS null OR patient_visit_id = :patientVisitId)"
			+ " AND (:patientTrackId IS null OR patient_track_id = :patientTrackId) ) prescriptionhistories"
			+ " WHERE row_number = 1 ORDER BY created_at ASC";

	@Query(value = GET_PRESCRIPTION_HISTORY, nativeQuery = true)
	public List<PrescriptionHistory> getPrescriptionHistory(@Param("prescriptionId") Long prescriptionId,
			@Param("patientVisitId") Long patientVisitId, @Param("patientTrackId") Long patientTrackId);


	@Query(value = GET_PRESCRIPTIONS , nativeQuery = true)
	public List<PrescriptionHistory> getPrescriptions(@Param("patientVisitId") Long patientVisitId);

}
