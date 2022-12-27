package com.mdtlabs.coreplatform.spiceadminservice.accountworkflow.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.dto.spice.SearchRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.AccountWorkflow;


/**
 * This interface maintains the CRUD operation for AccountWorkflow Entity
 *
 * @author Rajkumar
 */
public interface AccountWorkflowService {

	/**
	 * This method is used to add a new account workflow
	 * 
	 * @param accountWorkflow
	 * @return AccountWorkflow
	 * @author Rajkumar
	 */
	public AccountWorkflow addAccountWorkflow(AccountWorkflow accountWorkflow);

	/**
	 * This method is used to get the account workflow details based on country id
	 * and name
	 * 
	 * @param searchRequestDTO
	 * @return list of AccountWorkflow entity
	 * @author Rajkumar
	 */
	List<AccountWorkflow> getAccountWorkflows(SearchRequestDTO searchRequestDTO);

	/**
	 * This method is used to update the account workflow such as view screen
	 * 
	 * @param accountWorkflow
	 * @return AccountWorkflow entity
	 * @author Rajkumar
	 */
	public AccountWorkflow updateAccountWorkflow(AccountWorkflow accountWorkflow);

	/**
	 * This method is used to delete the account workflow by updating isDeleted
	 * column status
	 * 
	 * @param id
	 * @return boolean
	 * @author Rajkumar
	 */
	public boolean removeAccountWorkflow(long id);

	public List<AccountWorkflow> getAllAccountWorkFlows();


}
