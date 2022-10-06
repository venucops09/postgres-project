package com.mdtlabs.coreplatform.spiceservice.prescription.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.FillPrescriptionHistory;


@Repository
public interface FillPrescriptionHistoryRepository extends JpaRepository<FillPrescriptionHistory, Long> {

	public static final String GET_FILL_PRESCRIPTION_HISTORY = "SELECT * FROM fill_prescription_history"
			+ " WHERE patient_track_id = :patientTrackId AND patient_visit_id = :patientVisitId AND prescription_filled_days != :prescriptionFilledDays"
			+ " ORDER BY created_at ASC ";

	@Query(value = GET_FILL_PRESCRIPTION_HISTORY, nativeQuery = true)
	public List<FillPrescriptionHistory> getFillPrescriptionHistory(@Param("patientTrackId") Long patientTrackId,
			@Param("patientVisitId") Long patientVisitId, @Param("prescriptionFilledDays") int prescriptionFilledDays);

}
