package com.mdtlabs.coreplatform.spiceservice.metaData.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.model.entity.spice.Comorbidity;
import com.mdtlabs.coreplatform.common.service.Impl.GenericServiceImpl;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.ComorbidityRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.ComorbidityService;

@Service
public class ComorbidityServiceImpl extends GenericServiceImpl<Comorbidity> implements ComorbidityService {
	@Autowired
	ComorbidityRepository comorbidityRepository;

	public List<Comorbidity> findByIsDeletedFalseAndIsActiveTrue() {
		return comorbidityRepository.findByIsDeletedFalseAndIsActiveTrue();

	}

}
