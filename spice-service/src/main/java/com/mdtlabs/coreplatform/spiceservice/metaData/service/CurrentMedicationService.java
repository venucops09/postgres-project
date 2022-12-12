package com.mdtlabs.coreplatform.spiceservice.metaData.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.entity.spice.CurrentMedication;
import com.mdtlabs.coreplatform.common.service.GenericService;

public interface CurrentMedicationService extends GenericService<CurrentMedication> {

	List<CurrentMedication> findByIsDeletedFalseAndIsActiveTrue();

}
