package com.mdtlabs.coreplatform.spiceadminservice.data.controller;

import java.util.Arrays;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.logger.SpiceLogger;
import com.mdtlabs.coreplatform.common.model.dto.spice.CountryDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SubCountyDTO;
import com.mdtlabs.coreplatform.common.model.entity.Country;
import com.mdtlabs.coreplatform.common.model.entity.County;
import com.mdtlabs.coreplatform.common.model.entity.Subcounty;
import com.mdtlabs.coreplatform.spiceadminservice.data.service.DataService;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessResponse;

import io.swagger.annotations.Api;

import javax.validation.*;

/**
 * This controller class helps to perform actions on Country, Count and SubCounty Entities.
 *
 * @author Niraimathi S
 */
@RestController
@RequestMapping(value = "/data")
@Validated
@Api(basePath = "/data", value = "master_data", description = "Country related APIs", produces = "application/json")
public class DataController {

    private static final List<String> noDataList = Arrays.asList(Constants.NO_DATA_FOUND);

    @Autowired
    private DataService dataService;

    ModelMapper modelMapper = new ModelMapper();

    /**
     * Gets all countries.
     *
     * @param requestDTO
     * @return
     * @author Niraimathi S
     */
    @RequestMapping(value = "/country", method = RequestMethod.GET)
    public SuccessResponse<List<CountryDTO>> getAllCountries(@RequestBody RequestDTO requestDTO) {
        SpiceLogger.logInfo("Getting All Country Details");
        List<Country> countries = dataService.getAllCountries(requestDTO);

        if (!countries.isEmpty()) {
            List<CountryDTO> countryDtos = modelMapper.map(countries, new TypeToken<List<CountryDTO>>() {
            }.getType());
            return new SuccessResponse<List<CountryDTO>>(SuccessCode.GET_COUNTRIES, countryDtos, countryDtos.size(),
                    HttpStatus.OK);
        }
        return new SuccessResponse<List<CountryDTO>>(SuccessCode.GET_COUNTRIES, noDataList, 0, HttpStatus.OK);
    }

    /**
     * This method adds a new county.
     *
     * @param county
     * @return County entity.
     * @author Niraimathi S
     */
    @RequestMapping(value = "/county", method = RequestMethod.POST)
    public SuccessResponse<County> addCounty(@Valid @RequestBody County county) {
        SpiceLogger.logInfo("Adding a new country ");
        dataService.addCounty(county);
        return new SuccessResponse<>(SuccessCode.COUNTY_SAVE, HttpStatus.CREATED);
    }

    /**
     * This method gets a county from the database using id.
     *
     * @param id
     * @return
     * @author Niraimathi S
     */
    @RequestMapping(value = "/county/{id}", method = RequestMethod.GET)
    public SuccessResponse<County> getCountyById(@PathVariable(value = "id") long id) {
        SpiceLogger.logInfo("Get a county by ID");
        return new SuccessResponse<>(SuccessCode.GET_COUNTY, dataService.getCountyById(id), HttpStatus.OK);
    }

    /**
     * Used to retrieve all counties under a country based on country id.
     *
     * @param id
     * @return List of County entities.
     * @author Niraimathi S
     */
    @RequestMapping(value = "/county-list/{id}", method = RequestMethod.GET)
    public List<County> getAllCountyByCountryId(@PathVariable(value = "id") long id) {
        SpiceLogger.logInfo("Getting all County based on Country");
        List<County> counties = dataService.getAllCountyByCountryId(id);
//
//        if (counties.isEmpty()) {
//            return new SuccessResponse<List<County>>(SuccessCode.GET_COUNTIES, noDataList, 0, HttpStatus.OK);
////        }
        return counties;
    }

    /**
     * Used to update a county detail.
     *
     * @param county
     * @return updated county entity.
     * @author Niraimathi S
     */
    @RequestMapping(value = "/county", method = RequestMethod.PUT)
    public SuccessResponse<Boolean> updateCounty(@Valid @RequestBody County county) {
        SpiceLogger.logInfo("Updating County Details");
        dataService.updateCounty(county);
        return new SuccessResponse<>(SuccessCode.COUNTY_UPDATE, HttpStatus.OK);
    }

    /**
     * This method is used to add a new Country.
     *
     * @param country
     * @return Country Entity.
     * @author karthick M
     */
    @RequestMapping(method = RequestMethod.POST, value = "/country")
    public SuccessResponse<Country> createCountry(@Valid @RequestBody CountryDTO countryDTO) {
        dataService.createCountry(countryDTO);
        return new SuccessResponse<>(SuccessCode.COUNTRY_SAVE,
                HttpStatus.CREATED);
    }

    /**
     * Used to update a country detail like name, etc.,
     *
     * @param country
     * @return country Entity
     * @author Karthick M
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/country")
    public SuccessResponse<Country> updateCountry(@Valid @RequestBody Country country) {
        SpiceLogger.logInfo("Updates a Country");
        dataService.updateCountry(country);
        return new SuccessResponse<>(SuccessCode.COUNTRY_UPDATE,
                HttpStatus.OK);
    }

    /**
     * This method is used to add a new subcounty.
     *
     * @param subCounty
     * @return subcounty Entity.
     * @author karthick M
     */
    @RequestMapping(method = RequestMethod.POST, value = "/subcounty")
    public SuccessResponse<SubCountyDTO> createSubCounty(@Valid @RequestBody Subcounty subCounty) {
        SpiceLogger.logInfo("Creates a SubCounty based on Request");
        dataService.createSubCounty(subCounty);
        return new SuccessResponse<>(SuccessCode.SUBCOUNTY_SAVE, HttpStatus.OK);
    }

    /**
     * This method is used to retrieve single country details using countryId
     *
     * @param countryId
     * @return Country Entity
     * @author Karthick M
     */
    @RequestMapping(method = RequestMethod.GET, value = "/country/{id}")
    public SuccessResponse<Country> getCountryById(@PathVariable(value = "id") long countryId) {
        SpiceLogger.logInfo("Getting a Country details by on ID");
        return new SuccessResponse<Country>(SuccessCode.GET_COUNTRY, dataService.getCountryById(countryId),
                HttpStatus.OK);
    }

    /**
     * Used to update a subCounty detail like name, etc.,
     *
     * @return subCounty Entity
     * @author Karthick M
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/subcounty")
    public SuccessResponse<Subcounty> updateSubCountry(@Valid @RequestBody Subcounty subCounty) {
        SpiceLogger.logInfo("Updates a SUbCounty");
        dataService.updateSubCounty(subCounty);
        return new SuccessResponse<>(SuccessCode.SUBCOUNTY_UPDATE, HttpStatus.OK);
    }

    /**
     * This method is used to retrieve single countries details using countryId
     *
     * @param subCountyId
     * @return Country Entity
     * @author Karthick M
     */
    @RequestMapping(method = RequestMethod.GET, value = "/subcounty/{id}")
    public SuccessResponse<Subcounty> getSubCountyById(@PathVariable(value = "id") long subCountyId) {
        SpiceLogger.logInfo("Getting a list of County based on Country");
        return new SuccessResponse<Subcounty>(SuccessCode.GET_SUBCOUNTY, dataService.getSubCountyById(subCountyId),
                HttpStatus.OK);
    }

    /**
     * Gets all subcounties based on country and county id.
     *
     * @param countryId
     * @param countyId
     * @return List of SubCounty entities.
     * @author Niraimathi S
     */
    @RequestMapping(value = "/subcounty-list/{countryid}/{countyid}", method = RequestMethod.GET)
    public SuccessResponse<List<Subcounty>> getAllSubCounty(@PathVariable(value = "countryid") long countryId,
                                                            @PathVariable(value = "countyid") long countyId) {
        SpiceLogger.logInfo("Getting a SubCountry based on CountryId and countyId");
        List<Subcounty> subCounties = dataService.getAllSubCounty(countryId, countyId);
        if (subCounties.isEmpty()) {
            return new SuccessResponse<List<Subcounty>>(SuccessCode.GET_SUBCOUNTIES, noDataList, 0, HttpStatus.OK);
        }
        return new SuccessResponse<List<Subcounty>>(SuccessCode.GET_SUBCOUNTIES, subCounties, subCounties.size(),
                HttpStatus.OK);
    }
    
	@GetMapping(value = "/subcounty-list/{id}")
	public List<Subcounty> getAllSubCountyByCountryId(@PathVariable(value = "id") long countryId) {
		return dataService.getAllSubCountyByCountryId(countryId);
		
	}
	
	@GetMapping(value = "/get-country/{id}")
	public Country getCountry(@PathVariable(value = "id") long countryId) {
		return dataService.getCountryById(countryId);
	}

}