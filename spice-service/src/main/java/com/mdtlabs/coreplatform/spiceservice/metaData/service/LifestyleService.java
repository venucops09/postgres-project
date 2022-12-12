package com.mdtlabs.coreplatform.spiceservice.metaData.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.spice.Lifestyle;
import com.mdtlabs.coreplatform.common.service.GenericService;

public interface LifestyleService extends GenericService<Lifestyle> {

	List<Lifestyle> findByIsDeletedFalseAndIsActiveTrue();

}
