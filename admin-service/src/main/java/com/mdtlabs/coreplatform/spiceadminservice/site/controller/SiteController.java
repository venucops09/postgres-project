package com.mdtlabs.coreplatform.spiceadminservice.site.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.entity.Site;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceadminservice.site.service.SiteService;

import io.swagger.annotations.Api;

/**
 * This controller class maintains CRUD operation for site data.
 * 
 * @author Jeyaharini T A
 */

@RestController
@RequestMapping(value = "/site")
@Validated
@Api(basePath = "/site", value = "master_data", description = "Site related APIs", produces = "application/json")
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

}
