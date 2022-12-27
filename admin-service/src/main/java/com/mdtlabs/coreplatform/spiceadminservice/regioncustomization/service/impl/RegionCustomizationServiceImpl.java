package com.mdtlabs.coreplatform.spiceadminservice.regioncustomization.service.impl;

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
import com.mdtlabs.coreplatform.common.model.entity.spice.RegionCustomization;
import com.mdtlabs.coreplatform.spiceadminservice.regioncustomization.repository.RegionCustomizationRepository;
import com.mdtlabs.coreplatform.spiceadminservice.regioncustomization.service.RegionCustomizationService;


/**
 * This service class maintains the CRUD operations for region customization
 * 
 * @author Rajkumar
 *
 */
@Service
public class RegionCustomizationServiceImpl implements RegionCustomizationService {
	@Autowired
	RegionCustomizationRepository repository;

	ModelMapper modelMapper = new ModelMapper();

	/**
	 * {@inheritDoc}
	 */
	public RegionCustomization addCustomization(RegionCustomization regionCustomization) {
		if (Objects.isNull(regionCustomization)) {
			throw new BadRequestException(12006);
		}
		return repository.save(regionCustomization);
	}

	/**
	 * {@inheritDoc}
	 */
	public RegionCustomization getCustomization(CustomizationRequestDTO regionCustomizationRequestDTO) {
		RegionCustomization regionCustomization;
		if (Objects.isNull(regionCustomizationRequestDTO)) {
			throw new DataNotAcceptableException(12006);
		}

		if (Objects.isNull(regionCustomizationRequestDTO.getCountryId())) {
			throw new DataNotAcceptableException(10001);
		}

		regionCustomization = repository.findByCountryIdAndCategoryAndType(regionCustomizationRequestDTO.getCountryId(),
				regionCustomizationRequestDTO.getCategory(), regionCustomizationRequestDTO.getType(),
				Constants.BOOLEAN_FALSE);

		if (Objects.isNull(regionCustomization)) {
			throw new DataNotFoundException(22006);
		}
		return regionCustomization;
	}

	/**
	 * {@inheritDoc}
	 */
	public RegionCustomization updateCustomization(RegionCustomization regionCustomization) {
		if (Objects.isNull(regionCustomization)) {
			throw new BadRequestException(12006);
		}

		RegionCustomization existingRegionCustomization = repository.findById(regionCustomization.getId())
				.orElseThrow(() -> new DataNotFoundException(22006));

		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		modelMapper.map(regionCustomization, existingRegionCustomization);
		return repository.save(existingRegionCustomization);

	}
	
	public List<RegionCustomization> getRegionCustomizations(Map<String, Object> requestData) {
		List<String> regionCustomizationTypes = (List<String>) requestData.get("regionCustomizationTypes");
		List<String> regionConsentFormTypes = (List<String>) requestData.get("regionConsentFormTypes");
		
		String category =null;
//		
//		if (regionConsentFormTypes.isEmpty()) {
//			category = Constants.INPUT_FORM;
//			regionCustomizationTypes.removeAll(regionConsentFormTypes);
//		} else {
//			category = Constants.CONSENT_FORM;
//		}
		
		
		
		return repository.findByCategoryInAndTypeIn(regionConsentFormTypes, regionCustomizationTypes);
	}

}