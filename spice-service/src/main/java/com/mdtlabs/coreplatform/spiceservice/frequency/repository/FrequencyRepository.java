package com.mdtlabs.coreplatform.spiceservice.frequency.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Frequency;


/**
 * This repository maintains connection between database and frequency entity.
 *
 * @author Niraimathi S
 */
public interface FrequencyRepository extends JpaRepository<Frequency, Long> {

  // Frequency findByRiskLevelIgnoreCaseAndIsDeleted(String riskLevel, boolean b);

  /**
   * Finds Frequency by its name and type.
   *
   * @param name frequency name
   * @param type frequency type
   * @return Frequency Entity
   */
  Frequency findByNameIgnoreCaseAndTypeIgnoreCase(String name, String type);

  /**
   * Find Frequency by its id and isDeleted fields.
   *
   * @param id frequency Id
   * @param isDeleted isDeleted field.
   * @return Frequency entity.
   */
  Frequency findByIdAndIsDeleted(long id, boolean isDeleted);

  /**
   * Find list of frequencies by its risk level.
   *
   * @param riskLevel Frequency risk level
   * @return List of Frequecy Entities
   */
  List<Frequency> findAllByRiskLevel(String riskLevel);
}
