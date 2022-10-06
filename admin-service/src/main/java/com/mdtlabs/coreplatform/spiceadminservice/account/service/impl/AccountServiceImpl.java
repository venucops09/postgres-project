package com.mdtlabs.coreplatform.spiceadminservice.account.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.Account;
import com.mdtlabs.coreplatform.common.util.Pagination;
import com.mdtlabs.coreplatform.spiceadminservice.account.repository.AccountRepository;
import com.mdtlabs.coreplatform.spiceadminservice.account.service.AccountService;

/**
 * This service class maintains the CRUD operations for the account
 * 
 * @author Jeyaharini T A
 *
 */
@Transactional
@Service
public class AccountServiceImpl implements AccountService {

	@Autowired
	AccountRepository accountRepository;

	ModelMapper modelMapper = new ModelMapper();

	/**
	 * {@inheritDoc}
	 */

	@Override
	public Account addAccount(Account account) {
		if (Objects.isNull(account)) {
			throw new BadRequestException(12006);
		}

		if (!Objects.isNull(account.getClinicalWorkflows())) {
			if (account.getClinicalWorkflows().isEmpty()) {
				throw new BadRequestException(26009);
			}
			boolean containsNullOrEmpty = account.getClinicalWorkflows().stream()
					.anyMatch(workflow -> (Objects.isNull(workflow)));
			if (containsNullOrEmpty) {
				throw new BadRequestException(26009);
			}
		}
		return accountRepository.save(account);
	}

	/**
	 * {@inheritDoc}
	 */
	public Account updateAccount(Account account) {
		if (Objects.isNull(account)) {
			throw new BadRequestException(12006);
		}

		Account existingAccount = accountRepository.findByIdAndIsDeleted(account.getId(), false);

		if (Objects.isNull(existingAccount)) {
			throw new DataNotFoundException(26008);
		}

		if (!Objects.isNull(account.getClinicalWorkflows())) {
			if (account.getClinicalWorkflows().isEmpty()) {
				throw new BadRequestException(26009);
			}
			boolean containsNullOrEmpty = account.getClinicalWorkflows().stream()
					.anyMatch(workflow -> (Objects.isNull(workflow)));
			if (containsNullOrEmpty) {
				throw new BadRequestException(26009);
			}
		}

		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		modelMapper.map(account, existingAccount);
		return accountRepository.save(existingAccount);
//		accountRepository.updateAccount(account.getClinicalWorkflows(), account.getId());
//		return accountRepository.findById(account.getId()).orElseThrow();
	}

	/**
	 * {@inheritDoc}
	 */
	public Account getAccountById(long id) {

		Account account = accountRepository.findByIdAndIsActiveAndIsDeleted(id, true, false);
		if (Objects.isNull(account)) {
			throw new DataNotFoundException(26008);
		}
		return account;
	}

	/**
	 * {@inheritDoc}
	 */
	public Account activateDeactivateAccount(long id, boolean isActiveStatus) {

		Account accountToUpdate = accountRepository.findById(id).orElseThrow(() -> new DataNotFoundException(26008));
		accountToUpdate.setActive(isActiveStatus);
		return accountRepository.save(accountToUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Account> getDeactivatedAccounts(SearchRequestDTO searchRequestDTO) {

		String formattedSearchTerm = null;
		if (!Objects.isNull(searchRequestDTO.getSearchTerm()) && 0 < searchRequestDTO.getSearchTerm().length()) {
			formattedSearchTerm = searchRequestDTO.getSearchTerm().replaceAll("[^a-zA-Z0-9]*", "");
		}

		if (!Objects.isNull(searchRequestDTO.getIsPaginated()) && searchRequestDTO.getIsPaginated()) {
			Pageable pageable = Pagination.setPagination(searchRequestDTO.getPageNumber(), searchRequestDTO.getLimit(),
					FieldConstants.MODIFIED_AT, false);
			Page<Account> accounts = accountRepository.getDeactivatedAccountsWithPagination(formattedSearchTerm,
					pageable);
			return accounts.stream().collect(Collectors.toList());
		}

		List<Account> accounts = accountRepository.getDeactivatedAccounts(formattedSearchTerm);
		return accounts;
	}

}
