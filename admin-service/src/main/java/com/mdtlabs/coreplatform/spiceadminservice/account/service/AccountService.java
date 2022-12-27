package com.mdtlabs.coreplatform.spiceadminservice.account.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.dto.spice.SearchRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.Account;


/**
 * This service interface maintains the CRUD operations for the account
 * 
 * @author Rajkumar
 *
 */
public interface AccountService {

	/**
	 * To add a new account
	 * 
	 * @param account
	 * @return Account entity
	 * @author Rajkumar
	 */
	public Account addAccount(Account account);

	/**
	 * To update the existing account details by it's id
	 * 
	 * @param account
	 * @return Account entity
	 * @author Rajkumar
	 */
	public Account updateAccount(Account account);

	/**
	 * To get account by it's id
	 * 
	 * @param id
	 * @return Account entity
	 * @author Rajkumar
	 */
	public Account getAccountById(long id);

	/**
	 * To activate or deactivate account by updating isActive column
	 * 
	 * @param id
	 * @param isActive
	 * @return Account entity
	 * @author Rajkumar
	 */
	public Account activateDeactivateAccount(long id, boolean isActive);

	/**
	 * To get deactivated accounts
	 * 
	 * @param searchRequestDTO
	 * @return list of account entities
	 * @author Rajkumar
	 */
	public List<Account> getDeactivatedAccounts(SearchRequestDTO searchRequestDTO);
}
