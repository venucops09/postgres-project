package com.mdtlabs.coreplatform.spiceservice.metaData.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.controller.GenericController;
import com.mdtlabs.coreplatform.common.model.entity.spice.Lifestyle;

@RestController
@RequestMapping(value = "/lifestyle")
public class LifestyleController extends GenericController<Lifestyle> {

}
