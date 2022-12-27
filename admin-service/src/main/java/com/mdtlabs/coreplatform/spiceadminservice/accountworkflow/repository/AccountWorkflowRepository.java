package com.mdtlabs.coreplatform.spiceadminservice.accountworkflow.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.AccountWorkflow;


/**
 * This class maintains the connection between Entity and database.
 *
 * @author Rajkumar
 */
@Repository
public interface AccountWorkflowRepository extends JpaRepository<AccountWorkflow, Long> {
	public static final String GET_ACCOUT_WORKFLOWS = "SELECT accountworkflow FROM AccountWorkflow AS accountworkflow"
			+ " WHERE accountworkflow.countryId = :countryId AND accountworkflow.isDeleted=false AND accountworkflow.isActive=true "
			+ " AND (:searchTerm IS null OR lower(accountworkflow.name) LIKE CONCAT('%',lower(:searchTerm),'%') )";

	/**
	 * To check if there is any account workflow for the given country has the same
	 * name
	 * 
	 * @param name
	 * @param countryId
	 * @return boolean
	 * @author Rajkumar
	 */
	public boolean existsByNameIgnoreCaseAndCountryId(String name, long countryId);

	/**
	 * To find the account workflows by country id and search term
	 *
	 * @param countryid country id
	 * @param searchTerm search term
	 * @param pageable Pagination details
	 * @return AccountWorkflow Entity
	 * @author Rajkumar
	 */
	@Query(value = GET_ACCOUT_WORKFLOWS)
	public Page<AccountWorkflow> getAccountWorkflowsWithPagination(@Param("countryId") long countryid,
			@Param("searchTerm") String searchTerm, Pageable pageable);

	/**
	 * To find the account workflow by country id
	 * 
	 * @param countryid
	 * @param searchTerm
	 * @return AccountWorkflow Entity
	 * @author Rajkumar
	 */
	@Query(value = GET_ACCOUT_WORKFLOWS)
	public List<AccountWorkflow> getAllAccountWorkflows(@Param("countryId") long countryid,
			@Param("searchTerm") String searchTerm);

	/**
	 * To check the if the given account workflow exists or not
	 * 
	 * @param id
	 * @param isDeleted
	 * @return AccountWorkflow Entity
	 */
	public AccountWorkflow findByIdAndIsDeleted(long id, boolean isDeleted);
}
