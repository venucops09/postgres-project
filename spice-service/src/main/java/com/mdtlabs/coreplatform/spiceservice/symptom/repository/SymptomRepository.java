package com.mdtlabs.coreplatform.spiceservice.symptom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Symptom;

@Repository
public interface SymptomRepository extends JpaRepository<Symptom, Long> {
	public static final String GET_ALL_SYMPTOMS = " select symptoms from Symptom symptoms Order by symptoms.displayOrder DESC";

	@Query(value = GET_ALL_SYMPTOMS)
	List<Symptom> getAllSymptoms();
}
