package com.mdtlabs.coreplatform.spiceadminservice.labTest.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.LabTestResult;


@Repository
public interface LabTestResultRepository extends JpaRepository<LabTestResult, Long> {

//	public static final String GET_LABTESTID = "SELECT labtest_id FROM labtestresult WHERE id = :id";
//
//	@Query(value = GET_LABTESTID)
//	public Long getLabTestIdByResultId(@Param("id") long labTestResultId);

	public LabTestResult findByIdAndIsDeleted(long id, boolean isDeleted);

	public List<LabTestResult> findByLabTestIdAndIsDeletedAndIsActive(long labTestId, boolean isDeleted,
			boolean isActive);

}
