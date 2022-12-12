package com.mdtlabs.coreplatform.spiceservice.metaData.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.spice.PhysicalExamination;
import com.mdtlabs.coreplatform.common.service.GenericService;

public interface PhysicalExaminationService {

	List<PhysicalExamination> findByIsDeletedFalseAndIsActiveTrue();

}
