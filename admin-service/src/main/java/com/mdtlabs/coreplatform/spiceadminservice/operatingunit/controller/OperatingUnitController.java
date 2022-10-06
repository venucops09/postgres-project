package com.mdtlabs.coreplatform.spiceadminservice.operatingunit.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;

@RestController
@RequestMapping(value = "/operating-unit")
@Validated
@Api(basePath = "/operating-unit", value = "master_data", description = "Operating unit related APIs", produces = "application/json")
public class OperatingUnitController {

}
