package com.mdtlabs.coreplatform.spiceadminservice.accountworkflow.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.AccountWorkflow;
import com.mdtlabs.coreplatform.common.util.Pagination;
import com.mdtlabs.coreplatform.spiceadminservice.accountworkflow.repository.AccountWorkflowRepository;
import com.mdtlabs.coreplatform.spiceadminservice.accountworkflow.service.AccountWorkflowService;


/**
 * This is the service class which contains the business logic for manipulating AccountWorkFlow entity.
 *
 * @author Jeyaharini T A
 */
@Service
public class AccountWorkflowServiceImpl implements AccountWorkflowService {

	@Autowired
	AccountWorkflowRepository accountWorkflowRepository;

	/**
	 * {@inheritDoc}
	 */
	public AccountWorkflow addAccountWorkflow(AccountWorkflow accountWorkflow) {
		if (Objects.isNull(accountWorkflow)) {
			throw new BadRequestException(12006);
		}
		boolean containsNullorEmpty = accountWorkflow.getViewScreens().stream()
				.anyMatch(screen -> (Objects.isNull(screen) || screen.isBlank()));
		if (containsNullorEmpty) {
			throw new BadRequestException(25007);
		}
//		if (accountWorkflowRepository.existsByNameIgnoreCaseAndCountryId(accountWorkflow.getName(),
//				accountWorkflow.getCountryId())) {
//			throw new SpiceValidation(25006);
//		}

		accountWorkflow.setWorkflow(accountWorkflow.getName().toLowerCase().replaceAll(" ", "_"));
		accountWorkflow.setModuleType(FieldConstants.CUSTOMIZED);

		return accountWorkflowRepository.save(accountWorkflow);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<AccountWorkflow> getAccountWorkflows(SearchRequestDTO searchRequestDTO) {

		if (Objects.isNull(searchRequestDTO.getCountryId()) || 0 == searchRequestDTO.getCountryId()) {
			throw new DataNotAcceptableException(10001);
		}

		Pageable pageable = Pagination.setPagination(searchRequestDTO.getPageNumber(), searchRequestDTO.getLimit(),
				FieldConstants.NAME, true);

		String formattedSearchTerm = null;

		if (!Objects.isNull(searchRequestDTO.getSearchTerm()) && 0 < searchRequestDTO.getSearchTerm().length()) {
			formattedSearchTerm = searchRequestDTO.getSearchTerm().replaceAll("[^a-zA-Z0-9]*", "");
		}
		Page<AccountWorkflow> accountWorkflows = accountWorkflowRepository
				.getAccountWorkflowsWithPagination(searchRequestDTO.getCountryId(), formattedSearchTerm, pageable);
		return accountWorkflows.stream().collect(Collectors.toList());
	}

	/**
	 * {@inheritDoc}
	 */
	public AccountWorkflow updateAccountWorkflow(AccountWorkflow accountWorkflow) {

		if (Objects.isNull(accountWorkflow)) {
			throw new BadRequestException(12006);
		}

//		if (Objects.isNull(accountWorkflow.getViewScreens()) || accountWorkflow.getViewScreens().isEmpty()) {
//			throw new SpiceValidation(25007);
//		}
		boolean containsNullorEmpty = accountWorkflow.getViewScreens().stream()
				.anyMatch(screen -> (Objects.isNull(screen) || screen.isBlank()));
		if (containsNullorEmpty) {
			throw new BadRequestException(25007);
		}

		AccountWorkflow existingAccountWorkflow = accountWorkflowRepository.findById(accountWorkflow.getId())
				.orElseThrow(() -> new DataNotFoundException(25008));
		existingAccountWorkflow.setViewScreens(accountWorkflow.getViewScreens());
		accountWorkflow = accountWorkflowRepository.save(existingAccountWorkflow);
		return accountWorkflow;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeAccountWorkflow(long id) {

		if (Objects.isNull(id)) {
			throw new DataNotAcceptableException(12006);
		}
		AccountWorkflow existingAccountWorkflow = accountWorkflowRepository.findById(id)
				.orElseThrow(() -> new DataNotFoundException(25008));
		existingAccountWorkflow.setDeleted(true);
		accountWorkflowRepository.save(existingAccountWorkflow);
		return true;
	}

}
