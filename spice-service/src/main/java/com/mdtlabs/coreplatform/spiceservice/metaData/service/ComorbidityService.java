package com.mdtlabs.coreplatform.spiceservice.metaData.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.spice.Comorbidity;
import com.mdtlabs.coreplatform.common.service.GenericService;

public interface ComorbidityService extends GenericService<Comorbidity> {

	List<Comorbidity> findByIsDeletedFalseAndIsActiveTrue();

}
