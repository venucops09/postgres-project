package com.mdtlabs.coreplatform.spiceadminservice.data.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.dto.spice.CountryDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.CountryListDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.CountryOrganizationDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.Country;
import com.mdtlabs.coreplatform.common.model.entity.County;
import com.mdtlabs.coreplatform.common.model.entity.Subcounty;

/**
 * This interface contains business logic for manipulating Country, County and
 * SubCounty entities.
 *
 * @author Karthick M
 */
public interface DataService {

	/**
	 * This method is used to add a new Country.
	 *
	 * @param country
	 * @return Country Entity
	 * @author Karthick M
	 */
	Country createCountry(CountryOrganizationDTO countryDTO);

	/**
	 * This method is used to update a country details like name.
	 *
	 * @param country
	 * @return country Entity
	 * @author Karthick M
	 */
	Country updateCountry(Country country);

	/**
	 * This method is used to add a new county under a country.
	 *
	 * @param county
	 * @return County entity
	 * @author Niraimathi S
	 */
	County addCounty(County county);

	/**
	 * Used to get single county details using county id.
	 *
	 * @param id
	 * @return County entity
	 * @author Niraimathi S
	 */
	County getCountyById(long id);

	/**
	 * Gets all counties under a country using country id.
	 *
	 * @param id
	 * @return List of county entities.
	 * @author Niraimathi S
	 */
	List<County> getAllCountyByCountryId(long id);

	/**
	 * This method is used to update a county.
	 *
	 * @param county
	 * @return Updated county entity.
	 * @author Niraimathi S
	 */
	County updateCounty(County county);

	/**
	 * Used to get all countries and search countries using country name.
	 *
	 * @param requestDTO
	 * @return List of county entities
	 * @author Niraimathi S
	 */
	List<Country> getAllCountries(RequestDTO requestDTO);

	/**
	 * This method is used to add a new SubCounty.
	 *
	 * @param subCounty
	 * @return SubCounty Entity
	 * @author Karthick M
	 */
	Subcounty createSubCounty(Subcounty subCounty);

	/**
	 * Used to get single country details using country id.
	 *
	 * @param countryId
	 * @return Country entity
	 * @author Karthick M
	 */
	CountryOrganizationDTO getCountryById(long countryId);

	/**
	 * Used to soft delete subcounty details using subcounty id.
	 *
	 * @param subCounty
	 * @return subcounty entity
	 * @author Karthick M
	 */
	Subcounty updateSubCounty(Subcounty subCounty);

	/**
	 * Gets all subcounties based on country and county id.
	 *
	 * @param countryId
	 * @param countyId
	 * @return List of SubCounty entities.
	 * @author Niraimathi S
	 */
	List<Subcounty> getAllSubCounty(long countryId, long countyId);

	/**
	 * Used to get single country details using country id.
	 *
	 * @param id
	 * @return Country entity
	 * @author Karthick M
	 */
	Subcounty getSubCountyById(long id);

    /**
     * Gets country list with child organization counts
     * 
     * @param requestDTO request data
     * @return List of countryListDTO
     */
	List<Subcounty> getAllSubCountyByCountryId(Long countryId);

    /**
     * To get add subcounty list based on country id.
     * 
     * @param countryId country Id
     * @return List of Subcounty
     */
	List<CountryListDTO> getCountryList(RequestDTO requestDTO);
	
	/**
	 * Gets country by Id without users.
	 * 
	 * @param countryId country Id
	 * @return Country entity
	 */
	Country findCountryById(Long countryId);
}
