package com.mdtlabs.coreplatform.spiceadminservice.accountcustomization.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.CustomizationRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.AccountCustomization;
import com.mdtlabs.coreplatform.spiceadminservice.accountcustomization.service.AccountCustomizationService;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessResponse;

import io.swagger.annotations.Api;

import javax.validation.Valid;

/**
 * This controller class maintains CRUD operation for account customization
 * data.
 *
 * @author Jeyaharini T A
 */
@RestController
@RequestMapping(value = "/account-customization")
@Api(basePath = "/account-customization", value = "master_data", description = "Account Customization related APIs",
        produces = "application/json")
public class AccountCustomizationController {
    @Autowired
    AccountCustomizationService accountCustomizationService;

    /**
     * This method is used to add a account customization form data.
     *
     * @param accountCustomization
     * @return AccountCustomization
     * @author Jeyaharini T A
     */
    @PostMapping("/create")
    public SuccessResponse<AccountCustomization> addCustomization(
            @Valid @RequestBody AccountCustomization accountCustomization) {
        accountCustomizationService.addAccountCustomization(accountCustomization);
        return new SuccessResponse<>(SuccessCode.ACCOUNT_CUSTOMIZATION_SAVE, HttpStatus.CREATED);
    }

    /**
     * Get the account customization data details such as screening, enrollment and
     * consent forms based on account id and country id.
     *
     * @param customizationRequestDTO
     * @return AccountCustomization entity.
     * @author Jeyaharini T A
     */
    @GetMapping("/details")
    public SuccessResponse<AccountCustomization> getCustomization(
            @RequestBody CustomizationRequestDTO customizationRequestDTO) {
        return new SuccessResponse<AccountCustomization>(SuccessCode.GET_ACCOUNT_CUSTOMIZATION,
                accountCustomizationService.getCustomization(customizationRequestDTO), HttpStatus.OK);
    }

    /**
     * Update account customization data like screening, enrollment forms and
     * consent data based on id
     *
     * @param accountCustomization
     * @return AccountCustomization entity.
     * @author Jeyaharini T A
     */
    @PutMapping("/update")
    public SuccessResponse<AccountCustomization> updateCustomization(
            @Valid @RequestBody AccountCustomization accountCustomization) {
        accountCustomizationService.updateCustomization(accountCustomization);
        return new SuccessResponse<>(SuccessCode.ACCOUNT_CUSTOMIZATION_UPDATE, HttpStatus.OK);
    }

    /**
     * To remove account customization by updating is_deleted column
     *
     * @param requestMap
     * @return boolean
     * @author Jeyaharini T A
     */
    @PutMapping("/remove")
    public SuccessResponse<Boolean> removeCustomization(@RequestBody Map<String, Object> requestMap) {
        accountCustomizationService.removeCustomization(requestMap);
        return new SuccessResponse<>(SuccessCode.ACCOUNT_CUSTOMIZATION_DELETE, HttpStatus.OK);
    }
    
	@PostMapping("/static-data/get-list")
	public List<AccountCustomization> getAccountCustomizations(@RequestBody Map<String, Object> requestData) {
		return accountCustomizationService.getAccountCustomizations(requestData);
				
	}
}
