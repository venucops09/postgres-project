package com.mdtlabs.coreplatform.spiceadminservice.accountcustomization.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.dto.spice.CustomizationRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.AccountCustomization;
import com.mdtlabs.coreplatform.spiceadminservice.accountcustomization.repository.AccountCustomizationRepository;
import com.mdtlabs.coreplatform.spiceadminservice.accountcustomization.service.AccountCustomizationService;

/**
 * This service maintains the CRUD operations for account customization
 * 
 * @author Jeyaharini T A
 *
 */
@Service
public class AccountCustomizationServiceImpl implements AccountCustomizationService {

	@Autowired
	AccountCustomizationRepository accountCustomizationRepository;

	ModelMapper modelMapper = new ModelMapper();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountCustomization addAccountCustomization(AccountCustomization accountCustomization) {
		if (Objects.isNull(accountCustomization)) {
			throw new BadRequestException(12006);
		}
		return accountCustomizationRepository.save(accountCustomization);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountCustomization getCustomization(CustomizationRequestDTO customizationRequestDTO) {
		if (Objects.isNull(customizationRequestDTO)) {
			throw new DataNotAcceptableException(12006);
		}

		if (Objects.isNull(customizationRequestDTO.getCountryId())) {
			throw new DataNotAcceptableException(10001);
		}

		AccountCustomization accountCustomization = accountCustomizationRepository.getAccountCustomization(
				customizationRequestDTO.getCountryId(), customizationRequestDTO.getAccountId(),
				customizationRequestDTO.getCategory(), customizationRequestDTO.getType(),
				customizationRequestDTO.getClinicalWorkflowId(), Constants.BOOLEAN_FALSE);

		if (Objects.isNull(accountCustomization)) {
			throw new DataNotFoundException(24006);
		}
		return accountCustomization;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountCustomization updateCustomization(AccountCustomization accountCustomization) {
		if (Objects.isNull(accountCustomization)) {
			throw new BadRequestException(12006);
		}
		AccountCustomization existingAccountCustomization = accountCustomizationRepository
				.findByIdAndIsDeleted(accountCustomization.getId(), Constants.BOOLEAN_FALSE);

		if (Objects.isNull(existingAccountCustomization)) {
			throw new DataNotFoundException(24006);
		}

		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		modelMapper.map(accountCustomization, existingAccountCustomization);
		return accountCustomizationRepository.save(existingAccountCustomization);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeCustomization(Map<String, Object> requestMap) {
		long tenantId = requestMap.containsKey("tenantId") ? Long.parseLong(requestMap.get("tenantId").toString())
				: null;
		long id = requestMap.containsKey("id") ? Long.parseLong(requestMap.get("id").toString()) : null;

		accountCustomizationRepository.removeAccountCustomization(Constants.BOOLEAN_TRUE, tenantId, id);

		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<AccountCustomization> getAccountCustomizations(Map<String, Object> requestData) {
		Long countryId = Long.parseLong(requestData.get("countryId").toString());
		List<String> screenTypes = (List<String>) requestData.get("screenTypes");
		List<String> category = (List<String>) requestData.get("category");
		System.out.println("screenTypes" +screenTypes);
		System.out.println("category " + category);
		List<AccountCustomization> list = accountCustomizationRepository.findByCountryIdAndCategoryInAndTypeIn(countryId, category, screenTypes);
		System.out.println("AccountCustomizationlist " + list);
		return list;
	}

}
