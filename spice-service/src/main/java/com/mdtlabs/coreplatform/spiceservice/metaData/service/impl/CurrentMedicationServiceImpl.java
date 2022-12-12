package com.mdtlabs.coreplatform.spiceservice.metaData.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.model.entity.spice.CurrentMedication;
import com.mdtlabs.coreplatform.common.service.Impl.GenericServiceImpl;
import com.mdtlabs.coreplatform.spiceservice.metaData.repository.CurrentMedicationRespository;
import com.mdtlabs.coreplatform.spiceservice.metaData.service.CurrentMedicationService;

@Service
public class CurrentMedicationServiceImpl extends GenericServiceImpl<CurrentMedication>
		implements CurrentMedicationService {

	@Autowired
	CurrentMedicationRespository currentMedicationRespository;

	public List<CurrentMedication> findByIsDeletedFalseAndIsActiveTrue() {
		return currentMedicationRespository.findByIsDeletedFalseAndIsActiveTrue();
	}

}
