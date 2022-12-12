package com.mdtlabs.coreplatform.spiceservice.metaData.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.model.entity.spice.Lifestyle;
import com.mdtlabs.coreplatform.common.service.Impl.GenericServiceImpl;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.LifestyleRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.LifestyleService;

@Service
public class LifestyleServiceImpl extends GenericServiceImpl<Lifestyle> implements LifestyleService {

	@Autowired
	LifestyleRepository lifestyleRepository;

	public List<Lifestyle> findByIsDeletedFalseAndIsActiveTrue() {
		return lifestyleRepository.findByIsDeletedFalseAndIsActiveTrue();
	}
}
