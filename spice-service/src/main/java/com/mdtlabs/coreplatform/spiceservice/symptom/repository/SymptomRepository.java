package com.mdtlabs.coreplatform.spiceservice.symptom.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Symptom;


@Repository
public interface SymptomRepository extends JpaRepository<Symptom, Long> {
}
