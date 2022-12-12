package com.mdtlabs.coreplatform.spiceservice.metaData.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.spice.Complaints;
import com.mdtlabs.coreplatform.common.service.GenericService;

public interface ComplaintsService extends GenericService<Complaints> {

	List<Complaints> findByIsDeletedFalseAndIsActiveTrue();

}
