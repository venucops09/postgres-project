package com.mdtlabs.coreplatform.spiceadminservice.account.controller;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

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

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.Account;

import com.mdtlabs.coreplatform.spiceadminservice.account.service.AccountService;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessResponse;

/**
 * This controller class maintains CRUD operation for account data.
 * 
 * @author Rajkumar
 */

@RestController
@RequestMapping(value = "/account")
@Validated
public class AccountController {

	@Autowired
	AccountService accountService;

	private static final List<String> noDataList = Arrays.asList(Constants.NO_DATA_FOUND);

	/**
	 * To add a new account
	 * 
	 * @param account Account data to save
	 * @return Account entity
	 * @author Rajkumar
	 */
	@PostMapping("/create")
	public SuccessResponse<Account> addAccount(@Valid @RequestBody Account account) {
		accountService.addAccount(account);
		return new SuccessResponse<>(SuccessCode.ACCOUNT_SAVE,HttpStatus.CREATED);
	}

	/**
	 * To update the existing account by id
	 * 
	 * @param account Account data to update.
	 * @return Account entity
	 * @author Rajkumar
	 */
	@PutMapping("/update")
	public SuccessResponse<Account> updateAccount(@Valid @RequestBody Account account) {
		accountService.updateAccount(account);
		return new SuccessResponse<Account>(SuccessCode.ACCOUNT_UPDATE,
				HttpStatus.OK);
	}

	/**
	 * To get the account by its id
	 * 
	 * @param id Accoutn id to get account details.
	 * @return Account entity
	 * @author Rajkumar
	 */
	@GetMapping("/{id}")
	public SuccessResponse<Account> getAccountById(@PathVariable("id") long id) {
		return new SuccessResponse<Account>(SuccessCode.GET_ACCOUNT, accountService.getAccountById(id), HttpStatus.OK);
	}

	/**
	 * To activate the account by its id
	 * 
	 * @param id Account id to activate deactivated account.
	 * @return Account entity
	 * @author Rajkumar
	 */
	@GetMapping("/activate/{id}")
	public SuccessResponse<Account> activateAccountById(@PathVariable("id") long id) {
		accountService.activateDeactivateAccount(id, true);
		return new SuccessResponse<Account>(SuccessCode.ACCOUNT_ACTIVATE, HttpStatus.OK);
	}

	/**
	 * To de-activate the account by its id
	 * 
	 * @param id Account id to deactivate an active account
	 * @return Account entity
	 * @author Rajkumar
	 */
	@GetMapping("/deactivate/{id}")
	public SuccessResponse<Account> deactivateAccountById(@PathVariable("id") long id) {
		accountService.activateDeactivateAccount(id, false);
		return new SuccessResponse<Account>(SuccessCode.ACCOUNT_DEACTIVATE, HttpStatus.OK);
	}

	/**
	 * Gets all deactivated accounts.
	 *
	 * @param searchRequestDTO Request object containing search term and pagination information to get accounts.
	 * @return List of Account entities
	 * @author Rajkumar
	 */
	@GetMapping("/deactivate-list")
	public SuccessResponse<List<Account>> getAllDeactivedAccounts(@RequestBody SearchRequestDTO searchRequestDTO) {
		List<Account> deactivatedAccountsList = accountService.getDeactivatedAccounts(searchRequestDTO);
		if (!deactivatedAccountsList.isEmpty()) {
			return new SuccessResponse<List<Account>>(SuccessCode.GET_DEACTIVATE_ACCOUNT, deactivatedAccountsList,
					deactivatedAccountsList.size(), HttpStatus.OK);
		}
		return new SuccessResponse<List<Account>>(SuccessCode.GET_DEACTIVATE_ACCOUNT, noDataList, 0, HttpStatus.OK);
	}
	
	@GetMapping("/get-account/{id}")
	public Account getAccount(@PathVariable("id") long id) {
		return accountService.getAccountById(id);
	}
	
}
