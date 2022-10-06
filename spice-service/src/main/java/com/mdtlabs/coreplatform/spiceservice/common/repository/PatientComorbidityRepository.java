package com.mdtlabs.coreplatform.spiceservice.common.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientComorbidity;

/**
 * 
 * This class is a repository class to establish communication between database
 * and server side.
 * 
 * @author Niraimathi S
 *
 */
@Repository
public interface PatientComorbidityRepository extends JpaRepository<PatientComorbidity, Long> {
	
	public static final String GET_BY_TRACKER_ID = "select comorbidity from PatientComorbidity as comorbidity where comorbidity.patientTrackId = :patientTrackId AND comorbidity.isActive = :isActive AND comorbidity.isDeleted = :isDeleted AND (comorbidity.otherComorbidity is null)";
	
	/**
	 * Retrieves List of PatientComorbidity entities based on the patient tracker id , isActive and isDeleted values.
	 * 
	 * @param patientTrackId
	 * @param isActive
	 * @param isDeleted
	 * @return List of PatientComorbidity entities
	 */
	@Query(value = GET_BY_TRACKER_ID)
	public  List<PatientComorbidity> getBytrackerId(@Param("patientTrackId") Long patientTrackId, @Param("isActive") Boolean isActive, @Param("isDeleted") Boolean isDeleted);	
}
