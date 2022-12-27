package com.mdtlabs.coreplatform.spiceservice.common.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PhysicalExamination;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the PhysicalExamination module
 * action in database. In query annotation (nativeQuery = true) the below query
 * perform like SQL. Otherwise its perform like HQL default value for
 * nativeQuery FALSE
 * </p>
 * 
 * @author Rajkumar
 */
@Repository
public interface PhysicalExaminationRepository extends GenericRepository<PhysicalExamination> {

	public static final String GET_PHYSICAL_EXAMINATION_BY_IDS = "select physicalExamination from PhysicalExamination as physicalExamination where physicalExamination.id in (:physicalExaminationIds)";

	/**
	 * <p>
	 * This method used to get the Physical Examination using set of ids.
	 * </p>
	 * 
	 * @param physicalExaminationIds
	 * @return Set of PhysicalExamination Entity
	 */
	@Query(value = GET_PHYSICAL_EXAMINATION_BY_IDS)
	public Set<PhysicalExamination> getPhysicalExaminationByIds(
			@Param("physicalExaminationIds") Set<Long> physicalExaminationIds);

	List<PhysicalExamination> findByIsDeletedFalseAndIsActiveTrue();
}
