package com.mdtlabs.coreplatform.spiceservice.metaData.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.spice.Complication;
import com.mdtlabs.coreplatform.common.service.GenericService;

public interface ComplicationService extends GenericService<Complication> {

	List<Complication> findByIsDeletedFalseAndIsActiveTrue();

}
