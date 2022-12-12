package com.mdtlabs.coreplatform.spiceservice.metaData.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.model.entity.spice.Complication;
import com.mdtlabs.coreplatform.common.service.Impl.GenericServiceImpl;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.ComplicationRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.ComplicationService;

@Service
public class ComplicationServiceImpl extends GenericServiceImpl<Complication> implements ComplicationService {
	@Autowired
	ComplicationRepository complicationRepository;

	public List<Complication> findByIsDeletedFalseAndIsActiveTrue() {
		return complicationRepository.findByIsDeletedFalseAndIsActiveTrue();
	}

}
