package com.mdtlabs.coreplatform.spiceservice.metaData.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.controller.GenericController;
import com.mdtlabs.coreplatform.common.model.entity.spice.Comorbidity;

@RestController
@RequestMapping(value = "/comorbidity")
public class ComorbidityController extends GenericController<Comorbidity> {

}
