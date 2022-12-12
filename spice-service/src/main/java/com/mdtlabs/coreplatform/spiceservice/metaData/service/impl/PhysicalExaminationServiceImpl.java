package com.mdtlabs.coreplatform.spiceservice.metaData.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.model.entity.spice.PhysicalExamination;
import com.mdtlabs.coreplatform.spiceservice.common.repository.PhysicalExaminationRepository;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.PhysicalExaminationService;

@Service
public class PhysicalExaminationServiceImpl implements PhysicalExaminationService {

	@Autowired
	PhysicalExaminationRepository physicalExaminationRepository;

	public List<PhysicalExamination> findByIsDeletedFalseAndIsActiveTrue() {
		return physicalExaminationRepository.findByIsDeletedFalseAndIsActiveTrue();

	}
}
