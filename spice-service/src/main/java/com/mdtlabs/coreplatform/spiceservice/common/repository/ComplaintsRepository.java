package com.mdtlabs.coreplatform.spiceservice.common.repository;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Complaints;


/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the complaints module action in database.
 * In query annotation (nativeQuery = true) the below query perform like SQL.
 * Otherwise its perform like HQL default value for nativeQuery FALSE
 * </p>
 * 
 * @author Karthick Murugesan
 */
@Repository
public interface ComplaintsRepository extends JpaRepository<Complaints, Long> {
    public static final String GET_COMPLAINTS_BY_IDS = "select complaints from Complaints as complaints where complaints.id in (:complaintIds)";
    
    /**
	 * <p>
	 * This method used to get the Complaints using set of ids.
	 * </p>
	 * 
	 * @param physicalExaminationIds
	 * @return Set of PhysicalExamination Entity
	 */
	@Query(value = GET_COMPLAINTS_BY_IDS)
	public Set<Complaints> getComplaintsByIds(@Param("complaintIds") Set<Long> complaintIds);
}
