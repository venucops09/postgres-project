package com.mdtlabs.coreplatform.spiceadminservice.accountworkflow.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.SearchRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.AccountWorkflow;
import com.mdtlabs.coreplatform.spiceadminservice.accountworkflow.service.AccountWorkflowService;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessResponse;

import io.swagger.annotations.Api;

import java.util.List;

import javax.validation.Valid;

/**
 * This controller class maintains CRUD operation for account workflow data.
 * 
 * @author Jeyaharini T A
 */

@RestController
@RequestMapping(value = "/clinical-workflow")
@Validated
@Api(basePath = "/clinical-workflow", value = "master_data", description = "Account workflow related APIs", produces = "application/json")

public class AccountWorkflowController {

	@Autowired
	AccountWorkflowService accountWorkflowService;

	/**
	 * This method is used to add an account workflow data.
	 * 
	 * @param accountWorkflow
	 * @return AccountWorkflow
	 * @author Jeyaharini T A
	 */
	@PostMapping("/create")
	public SuccessResponse<AccountWorkflow> addAccountWorkflow(@Valid @RequestBody AccountWorkflow accountWorkflow) {
		accountWorkflowService.addAccountWorkflow(accountWorkflow);
		return new SuccessResponse<>(SuccessCode.ACCOUNT_WORKFLOW_SAVE, HttpStatus.CREATED);
	}

	/**
	 * This method is used to retrieve account workflow details with the condition
	 * 
	 * @param searchRequestDTO
	 * @return Account workflow entity
	 * @author Jeyaharini T A
	 */
	@GetMapping("/list")
	public SuccessResponse<AccountWorkflow> getAccountWorkflows(@RequestBody SearchRequestDTO searchRequestDTO) {
		return new SuccessResponse<AccountWorkflow>(SuccessCode.GET_ACCOUNT_WORKFLOW,
				accountWorkflowService.getAccountWorkflows(searchRequestDTO), HttpStatus.OK);
	}

	/**
	 * Used to update an account workflow view screens
	 * 
	 * @param accountWorkflow workflow
	 * @return Account workflow Entity
	 * @author Jeyaharini T A
	 */
	@PutMapping("/update")
	public SuccessResponse<AccountWorkflow> updateAccountWorkflow(@Valid @RequestBody AccountWorkflow accountWorkflow) {
		accountWorkflowService.updateAccountWorkflow(accountWorkflow);
		return new SuccessResponse<>(SuccessCode.ACCOUNT_WORKFLOW_UPDATE, HttpStatus.OK);
	}

	/**
	 * Used to delete an account workflow by its id.
	 * 
	 * @param id
	 * @return AccountWorkflow entity
	 * @author Jeyaharini T A
	 */
	@PutMapping("/remove/{id}")
	public SuccessResponse<AccountWorkflow> deleteAccountWorkflow(@PathVariable("id") long id) {
		accountWorkflowService.removeAccountWorkflow(id);
		return new SuccessResponse<>(SuccessCode.ACCOUNT_WORKFLOW_DELETE, HttpStatus.OK);
	}
	
	/**
	 * Gets all wokrflows
	 * 
	 * @return List of AccountWorkflows
	 */
	@GetMapping("/get-all-workflows")
	public List<AccountWorkflow> getAllAccountWorkFlows() {
		return accountWorkflowService.getAllAccountWorkFlows();
	}

}
