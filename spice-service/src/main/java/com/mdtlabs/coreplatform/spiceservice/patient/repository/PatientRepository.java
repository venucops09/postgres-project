package com.mdtlabs.coreplatform.spiceservice.patient.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Patient;


@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

//	public static final String SET_VIRTUAL_ID = "UPDATE patient SET virtual_id = (CASE WHEN (SELECT virtual_id FROM patient"
//			+ " WHERE tenant_id = :tenantId limit 1) IS NOT NULL"
//			+ " THEN ((SELECT  virtual_id FROM patient WHERE tenant_id = :tenantId  AND id != :id"
//			+ " ORDER BY virtual_id DESC limit 1) + 1) ELSE 1 END) WHERE tenant_id = :tenantId AND id = :id";
//
//	@Procedure(value = "UPDATE_VIRTUAL_ID", name = "UPDATE_VIRTUAL_ID", outputParameterName = "patient_virtualId")
//	long updateVirualId(@Param("patient_id") long id, @Param("patient_tenantId") long tenantId);

	public static final String REMOVE_PATIENT = "UPDATE Patient SET isDeleted = :isDeleted WHERE id = :id";

	@Modifying
	@Transactional
	@Query(value = REMOVE_PATIENT)
	public void removePatient(@Param("isDeleted") boolean isDeleted, @Param("id") long id);

}
