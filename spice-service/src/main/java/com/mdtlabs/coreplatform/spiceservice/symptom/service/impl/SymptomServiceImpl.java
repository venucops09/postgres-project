package com.mdtlabs.coreplatform.spiceservice.symptom.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.model.entity.spice.Symptom;
import com.mdtlabs.coreplatform.spiceservice.symptom.repository.SymptomRepository;
import com.mdtlabs.coreplatform.spiceservice.symptom.service.SymptomService;

import java.util.List;

@Service
public class SymptomServiceImpl implements SymptomService {

	@Autowired
	SymptomRepository symptomRepository;

	@Override
	public List<Symptom> getAllSymptoms() {
		List<Symptom> symptomList = symptomRepository.findAll();
		return symptomList;
	}

	@Override
	public Symptom addSymptom(Symptom symptom) {
		if (null == symptom) {
			throw new BadRequestException(1000);
		}
		Symptom symptomResponse = symptomRepository.save(symptom);
		return symptomResponse;
	}
}
