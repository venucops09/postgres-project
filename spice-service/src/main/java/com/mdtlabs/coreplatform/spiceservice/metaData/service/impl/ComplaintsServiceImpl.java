package com.mdtlabs.coreplatform.spiceservice.metaData.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.model.entity.spice.Complaints;
import com.mdtlabs.coreplatform.common.service.Impl.GenericServiceImpl;
import com.mdtlabs.coreplatform.spiceservice.common.repository.ComplaintsRepository;
//import com.mdtlabs.coreplatform.spiceservice.metaData.service.ComplaintsService;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.ComplaintsService;

@Service
public class ComplaintsServiceImpl extends GenericServiceImpl<Complaints> implements ComplaintsService {
	@Autowired
	ComplaintsRepository complaintsRepository;

	public List<Complaints> findByIsDeletedFalseAndIsActiveTrue() {
		return complaintsRepository.findByIsDeletedFalseAndIsActiveTrue();
	}

}
