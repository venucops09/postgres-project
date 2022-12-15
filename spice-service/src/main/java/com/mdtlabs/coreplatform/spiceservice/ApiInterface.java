package com.mdtlabs.coreplatform.spiceservice;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.mdtlabs.coreplatform.common.model.dto.spice.OtherMedicationDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.Account;
import com.mdtlabs.coreplatform.common.model.entity.Country;
import com.mdtlabs.coreplatform.common.model.entity.County;
import com.mdtlabs.coreplatform.common.model.entity.Program;
import com.mdtlabs.coreplatform.common.model.entity.Site;
import com.mdtlabs.coreplatform.common.model.entity.Subcounty;
import com.mdtlabs.coreplatform.common.model.entity.spice.AccountCustomization;
import com.mdtlabs.coreplatform.common.model.entity.spice.AccountWorkflow;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTest;
import com.mdtlabs.coreplatform.common.model.entity.spice.RegionCustomization;

/**
 * This interface is used to access admin service APIs.
 * 
 * @author Niraimathi S
 *
 */
//@FeignClient(value = "AdminServiceFeign", url = "http://192.168.178.174/admin-service")
@FeignClient(name = "admin-service")
public interface ApiInterface {

	@GetMapping("/medication/other-medication/{countryId}")
	public ResponseEntity<OtherMedicationDTO> getOtherMedication(@RequestHeader("Authorization") String token,
			@PathVariable long countryId);

	@PostMapping("/labtest/patient-labtest/get-by-name")
	public ResponseEntity<LabTest> getLabTestByName(@RequestHeader("Authorization") String token,
			@RequestBody SearchRequestDTO searchRequestDTO);

	@PostMapping("/labtest/patient-labtest/get-list-by-ids")
	public ResponseEntity<List<LabTest>> getLabTestsByIds(@RequestHeader("Authorization") String token,
			@RequestBody Set<Long> labTestIds);

	@GetMapping("/labtest/labtest-result/{id}")
	public ResponseEntity<Map> getLabTestResultsByLabTestId(@RequestHeader("Authorization") String token,
			@PathVariable long id);

	@GetMapping("/data/get-country/{id}")
	public Country getCountryById(@RequestHeader("Authorization") String token, @PathVariable(value = "id") long id);

	@GetMapping(value = "/data/county-list/{id}")
	public List<County> getAllCountyByCountryId(@RequestHeader("Authorization") String token,
			@PathVariable(value = "id") long id);

	@GetMapping(value = "/data/subcounty-list/{id}")
	public List<Subcounty> getAllSubCountyByCountryId(@RequestHeader("Authorization") String token,
			@PathVariable(value = "id") long id);

	@PostMapping(value = "/site/get-sites-by-tenants")
	public List<Site> getSitesByTenantIds(@RequestHeader("Authorization") String token,
			@RequestBody List<Long> tenants);

	@GetMapping("/account/get-account/{id}")
	public Account getAccountById(@RequestHeader("Authorization") String token, @PathVariable(value = "id") long id);

	@GetMapping("/clinical-workflow/get-all-workflows")
	public List<AccountWorkflow> getAllAccountWorkFlows(@RequestHeader("Authorization") String token);

	@PostMapping("/account-customization/static-data/get-list")
	public List<AccountCustomization> getAccountCustomization(@RequestHeader("Authorization") String token,
			@RequestBody Map<String, Object> requestData);

	@PostMapping("/region-customization/static-data/get-list")
	public List<RegionCustomization> getRegionCustomizations(@RequestHeader("Authorization") String token,
			Map<String, Object> requestData);

	@GetMapping("/site/get-by-ou-id/{operatingUnitId}")
	public List<Site> getSitesByOperatingUnitId(@RequestHeader("Authorization") String token,
			@PathVariable(value = "operatingUnitId") Long operatingUnitId);

	@PostMapping("program/get-by-site-ids")
	public List<Program> getPrograms(@RequestHeader("Authorization") String token, @RequestBody List<Long> siteIds);

	@PostMapping(value = "/site/{id}")
	public Site getSiteById(@RequestHeader("Authorization") String token, @RequestBody Long siteId);
}
