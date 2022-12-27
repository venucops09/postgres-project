package com.mdtlabs.coreplatform.spiceadminservice.site.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.entity.Site;

import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceadminservice.site.service.SiteService;


/**
 * This controller class maintains CRUD operation for site data.
 * 
 * @author Rajkumar
 */

@RestController
@RequestMapping(value = "/site")
@Validated
public class SiteController {

	@Autowired
	SiteService siteService;

	@PostMapping
//	@TokenParse
	public SuccessResponse<Site> addSite(@RequestBody Site site) {
		return new SuccessResponse<Site>(SuccessCode.SITE_SAVE, siteService.addSite(site), HttpStatus.OK);
	}

	@PutMapping
//	@TokenParse
	public SuccessResponse<Site> updateSite(@RequestBody Site site) {
		return new SuccessResponse<Site>(SuccessCode.SITE_UPDATE, siteService.updateSite(site), HttpStatus.OK);
	}
	
	/**
	 * Gets Sites based on tenant its tenant Ids.
	 * 
	 * @param tenants list if tenant IDs
	 * @return List of Site Entities
	 */
	@PostMapping(value = "/get-sites-by-tenants")
	public List<Site> getSitesByTenantIds(@RequestBody List<Long> tenants) {
		return siteService.getSitesByTenantIds(tenants);
	}
	
	/**
	 * Gets Sites based on operating unit Id
	 * 
	 * @param operatingUnitId operating unit Id
	 * @return List of Site Entities
	 */
	@GetMapping(value = "/get-by-ou-id/{operatingUnitId}")
	public List<Site> getSitesByOperatingUnitId(@PathVariable(value = "operatingUnitId") Long operatingUnitId) {
		return siteService.getSitesByOperatingUnitId(operatingUnitId);
	}
	
	
	/**
	 * Gets a site using id and isDeleted fields
	 * 
	 * @param siteId site id
	 * @return Site entity
	 */
	@GetMapping(value = "/{siteId}")
	public Site getSiteById(@PathVariable("siteId") Long siteId) {
		return siteService.getSiteById(siteId);
	}


}
