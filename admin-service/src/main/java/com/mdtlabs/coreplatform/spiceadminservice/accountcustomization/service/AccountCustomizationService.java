package com.mdtlabs.coreplatform.spiceadminservice.accountcustomization.service;

import java.util.Map;

import com.mdtlabs.coreplatform.common.model.dto.spice.CustomizationRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.AccountCustomization;

/**
 * This interface maintains the CRUD operations for account customization
 * 
 * @author Jeyaharini T A
 *
 */
public interface AccountCustomizationService {

	/**
	 * To add a new account customization data
	 * 
	 * @param accountCustomization
	 * @return AccountCustomization
	 * @author Jeyaharini T A
	 */
	public AccountCustomization addAccountCustomization(AccountCustomization accountCustomization);

	/**
	 * To get account customization data like screening, enrollment forms and
	 * consent data based on conditions such as account id, country id etc.,
	 * 
	 * @param customizationRequestDTO
	 * @return AccountCustomization
	 * @author Jeyaharini T A
	 */
	public AccountCustomization getCustomization(CustomizationRequestDTO customizationRequestDTO);

	/**
	 * Update account customization data like screening, enrollment forms and
	 * consent data based on account id and region customization id
	 * 
	 * @param accountCustomization
	 * @return Count of rows updated.
	 * @author Jeyaharini T A
	 */
	public AccountCustomization updateCustomization(AccountCustomization accountCustomization);

	/**
	 * To remove the account customization by updating is_deleted field based on id
	 * 
	 * @param requestMap
	 * @return AccountCustomization
	 * @author Jeyaharini T A
	 */
	public boolean removeCustomization(Map<String, Object> requestMap);

}
