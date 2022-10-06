package com.mdtlabs.coreplatform.spiceservice.prescription.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Prescription;

/**
 * This repository will maintains the custom functions for Prescription
 * 
 * @author Jeyaharini T A
 *
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

	public static final String GET_PRESCRIPTIONS = "SELECT * FROM prescription WHERE Id IN (:ids)";

	public static final String PRESCRIPTION_COUNT = 
	"Select count(*) from Prescription as prescription where prescription.isDeleted=false and prescription.endDate <= :endDate and prescription.patientTrackId = :patientTrackId ";
	
	/**
	 * To get the prescriptions by its id's
	 * 
	 * @param ids
	 * @param isDeleted
	 * @return List of Prescription
	 */
	@Query(value = GET_PRESCRIPTIONS, nativeQuery = true)
	public List<Prescription> findByIds(@Param("ids") Set<Long> ids);
	
	/**
	 * To get the prescriptions by patient track id and deleted status
	 * @param patientTrackId
	 * @param isDeleted
	 * @return
	 */
	public List<Prescription> findByPatientTrackIdAndIsDeleted(long patientTrackId, boolean isDeleted);


	/**
	 * To get the prescriptions by patient track id and deleted status
	 * @param patientTrackId
	 * @param isDeleted
	 * @return
	 */
	public List<Prescription> findByPatientTrackIdAndPatientVisitIdAndIsDeleted(Long patientTrackId, Long patientVisitId, boolean isDeleted);

	@Query(value = PRESCRIPTION_COUNT)
	public int getPrecriptionCount(@Param("endDate") Date endDate, @Param("patientTrackId") Long patientTrackId);
}
