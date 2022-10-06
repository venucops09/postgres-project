package com.mdtlabs.coreplatform.spiceservice.prescription.repository;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.FillPrescription;

@Repository
public interface FillPrescriptionRepository extends JpaRepository<FillPrescription, Long> {

	public static final String GET_FILL_PRESCRIPTIONS_BY_ID = "SELECT * FROM fill_prescription WHERE prescription_id IN (:ids)";

	public static final String REMOVE_FILL_PRESCRIPTIONS = "UPDATE FillPrescription SET isDeleted = :isDeleted WHERE prescriptionId = :prescriptionId AND tenantId = :tenantId";

//	public static final String GET_FILL_PRESCRIPTIONS = "SELECT FillPrescription FROM FillPrescription WHERE patientTrackId = :patientTrackId AND remainingPrescriptionDays > 0 ";

	public static final String GET_FILL_PRESCRIPTIONS = "SELECT * FROM fill_prescription WHERE id IN (:ids) AND is_deleted = :isDeleted";

	/**
	 * To get the prescriptions by its id's
	 * 
	 * @param ids
	 * @param isDeleted
	 * @return List of Prescription
	 */
	@Query(value = GET_FILL_PRESCRIPTIONS_BY_ID, nativeQuery = true)
	public List<FillPrescription> findByPrescriptionIds(@Param("ids") Set<Long> ids);

	@Modifying
	@Transactional
	@Query(value = REMOVE_FILL_PRESCRIPTIONS)
	public List<FillPrescription> removeFillPrescriptions(@Param("isDeleted") boolean isDeleted,
			@Param("prescriptionId") Long prescriptionId, @Param("tenantId") Long tenantId);

//	@Query(value = GET_FILL_PRESCRIPTIONS)
//	public List<FillPrescription> getFillPrescriptions(@Param("patientTrackId") long patientTrackId);

	public List<FillPrescription> findByPatientTrackIdAndRemainingPrescriptionDaysGreaterThan(long patientTrackId,
			int remainingPrescriptionDays);

	@Query(value = GET_FILL_PRESCRIPTIONS, nativeQuery = true)
	public List<FillPrescription> findByIdsAndIsDeleted(@Param("ids") List<Long> fillPrescriptionIds,
			@Param("isDeleted") boolean isDeleted);
}
