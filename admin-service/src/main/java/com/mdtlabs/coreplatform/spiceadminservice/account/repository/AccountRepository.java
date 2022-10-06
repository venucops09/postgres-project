package com.mdtlabs.coreplatform.spiceadminservice.account.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mdtlabs.coreplatform.common.model.entity.Account;


/**
 * This repository contains the needed customized functions for account.
 * 
 * @author Jeyaharini T A
 *
 */
@Repository
@Transactional
public interface AccountRepository extends JpaRepository<Account, Long> {

	public static final String GET_DEACTIVATED_ACCOUNTS = "SELECT account FROM Account AS account WHERE "
			+ " (:searchTerm IS null OR lower(account.name) LIKE CONCAT('%',lower(:searchTerm),'%')) "
			+ " AND account.isActive=false AND account.isDeleted = false ";

	public static final String UPDATE_ACCOUNT = "UPDATE Account AS account SET account.clinicalWorkflows = :clinicalWorkflows WHERE account.id = :id";

	public static final String ACCOUNT_DETAILS = "SELECT account FROM Account WHERE ";
	
	/**
	 * To get the account by id
	 * 
	 * @param id
	 * @param isDeleted
	 * @return Account entity
	 * @author Jeyaharini T A
	 */
	public Account findByIdAndIsDeleted(long id, boolean isDeleted);

	/**
	 * To get the account by id
	 * 
	 * @param id
	 * @param isActive
	 * @param isDeleted
	 * @return Account entity
	 * @author Jeyaharini T A
	 */
	public Account findByIdAndIsActiveAndIsDeleted(long id, boolean isActive, boolean isDeleted);

	/**
	 * To get the deactivted accounts with pagination
	 * 
	 * @param searchTerm
	 * @param pageable
	 * @return Page of account entities
	 * @author Jeyaharini T A
	 */
	@Query(value = GET_DEACTIVATED_ACCOUNTS)
	public Page<Account> getDeactivatedAccountsWithPagination(@Param("searchTerm") String searchTerm,
			Pageable pageable);

	/**
	 * To get the deactivated accounts
	 * 
	 * @param searchTerm
	 * @return List of account entities
	 * @author Jeyaharini T A
	 */
	@Query(value = GET_DEACTIVATED_ACCOUNTS)
	public List<Account> getDeactivatedAccounts(@Param("searchTerm") String searchTerm);

	/**
	 * To update Account's clinical workflows.
	 * @param clinicalWorkflows
	 * @param id
	 */
	@Modifying
	@Query(value = UPDATE_ACCOUNT)
	public void updateAccount(@Param("clinicalWorkflows") List<String> clinicalWorkflows, @Param("id") long id);
}
