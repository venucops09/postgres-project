package com.mdtlabs.coreplatform.spiceservice.metaData.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.controller.GenericController;
import com.mdtlabs.coreplatform.common.model.entity.spice.PhysicalExamination;

@RestController
@RequestMapping(value = "/physical-examination")
public class PhysicalExaminationController extends GenericController<PhysicalExamination> {

}
