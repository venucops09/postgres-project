package com.mdtlabs.coreplatform.spiceadminservice.regioncustomization.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.CustomizationRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.RegionCustomization;

import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceadminservice.regioncustomization.service.RegionCustomizationService;

/**
 * This controller class maintains CRUD operation for region customization data.
 * 
 * @author Rajkumar
 */
@RestController
@RequestMapping(value = "/region-customization")
@Validated
public class RegionCustomizationController {

	@Autowired
	RegionCustomizationService regionCustomizationService;

	/**
	 * This method is used to add a country customization form data.
	 * 
	 * @param regionCustomization
	 * @return RegionCustomization
	 * @author Rajkumar
	 */
	@RequestMapping(method = RequestMethod.POST, path = "/create")
	public SuccessResponse<RegionCustomization> addCustomization(@Valid @RequestBody RegionCustomization regionCustomization) {
		regionCustomizationService.addCustomization(regionCustomization);
		return new SuccessResponse<RegionCustomization>(SuccessCode.REGION_CUSTOMIZATION_SAVE, HttpStatus.OK);
	}

	/**
	 * Get the region customization data details such as screening, enrollment and
	 * consent forms based on region organization id.
	 * 
	 * @param regionCustomizationRequestDTO
	 * @return RegionCustomization entity.
	 * @author Rajkumar
	 */
	@RequestMapping(method = RequestMethod.GET, path = "/details")
	public SuccessResponse<RegionCustomization> getCustomization(
			@RequestBody CustomizationRequestDTO regionCustomizationRequestDTO) {
		return new SuccessResponse<RegionCustomization>(SuccessCode.GET_REGION_CUSTOMIZATION,
				regionCustomizationService.getCustomization(regionCustomizationRequestDTO), HttpStatus.OK);
	}

	/**
	 * Update region customization data like screening, enrollment forms and consent
	 * data based on region
	 * 
	 * @param regionCustomization
	 * @return RegionCustomization entity.
	 * @author Rajkumar
	 */
	@RequestMapping(method = RequestMethod.PUT, path = "/update")
	public SuccessResponse<RegionCustomization> updateCustomization(
			@Valid @RequestBody RegionCustomization regionCustomization) {
		regionCustomizationService.updateCustomization(regionCustomization);
		return new SuccessResponse<RegionCustomization>(SuccessCode.REGION_CUSTOMIZATION_UPDATE, HttpStatus.OK);
	}
	
	/**
	 * Gets list of customizatons 
	 * 
	 * @param requestData
	 * @return List of RegionCustomization
	 */
	@PostMapping("/static-data/get-list")
	public List<RegionCustomization> getRegionCustomizations(@RequestBody Map<String, Object> requestData) {
		return regionCustomizationService.getRegionCustomizations(requestData);
	}


}
