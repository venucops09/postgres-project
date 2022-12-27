package com.mdtlabs.coreplatform.spiceadminservice.data.service;

import java.util.List;
import java.util.Map;

import com.mdtlabs.coreplatform.common.model.dto.spice.CountryOrganizationDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.Country;
import com.mdtlabs.coreplatform.common.model.entity.County;
import com.mdtlabs.coreplatform.common.model.entity.Subcounty;

/**
 * This interface contains business logic for manipulating Country, County and
 * SubCounty entities.
 *
 * @author Rajkumar
 */
public interface DataService {

	/**
	 * This method is used to add a new Country.
	 *
	 * @param country
	 * @return Country Entity
	 * @author Rajkumar
	 */
	Country createCountry(CountryOrganizationDTO countryDTO);

	/**
	 * This method is used to update a country details like name.
	 *
	 * @param country
	 * @return country Entity
	 * @author Rajkumar
	 */
	Country updateCountry(Country country);

	/**
	 * This method is used to add a new county under a country.
	 *
	 * @param county
	 * @return County entity
	 * @author Rajkumar
	 */
	County addCounty(County county);

	/**
	 * Used to get single county details using county id.
	 *
	 * @param id
	 * @return County entity
	 * @author Rajkumar
	 */
	County getCountyById(long id);

	/**
	 * Gets all counties under a country using country id.
	 *
	 * @param id
	 * @return List of county entities.
	 * @author Rajkumar
	 */
	List<County> getAllCountyByCountryId(long id);

	/**
	 * This method is used to update a county.
	 *
	 * @param county
	 * @return Updated county entity.
	 * @author Rajkumar
	 */
	County updateCounty(County county);

	/**
	 * Used to get all countries and search countries using country name.
	 *
	 * @param requestDTO
	 * @return List of county entities
	 * @author Rajkumar
	 */
	List<Country> getAllCountries(RequestDTO requestDTO);

	/**
	 * This method is used to add a new SubCounty.
	 *
	 * @param subCounty
	 * @return SubCounty Entity
	 * @author Rajkumar
	 */
	Subcounty createSubCounty(Subcounty subCounty);

	/**
	 * Used to get single country details using country id.
	 *
	 * @param countryId
	 * @return Country entity
	 * @author Rajkumar
	 */
	CountryOrganizationDTO getCountryById(long countryId);

	/**
	 * Used to soft delete subcounty details using subcounty id.
	 *
	 * @param subCounty
	 * @return subcounty entity
	 * @author Rajkumar
	 */
	Subcounty updateSubCounty(Subcounty subCounty);

	/**
	 * Gets all subcounties based on country and county id.
	 *
	 * @param countryId
	 * @param countyId
	 * @return List of SubCounty entities.
	 * @author Rajkumar
	 */
	List<Subcounty> getAllSubCounty(long countryId, long countyId);

	/**
	 * Used to get single country details using country id.
	 *
	 * @param id
	 * @return Country entity
	 * @author Rajkumar
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
	Map<String, Object> getCountryList(RequestDTO requestDTO);
	
	/**
	 * Gets country by Id without users.
	 * 
	 * @param countryId country Id
	 * @return Country entity
	 */
	Country findCountryById(Long countryId);
}
