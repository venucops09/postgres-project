package com.mdtlabs.coreplatform.spiceservice.frequency.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.model.entity.spice.Frequency;
import com.mdtlabs.coreplatform.spiceservice.frequency.repository.FrequencyRepository;
import com.mdtlabs.coreplatform.spiceservice.frequency.service.FrequencyService;


/**
 * This class implements the Frequency interface and contains actual
 * business logic to perform operations on frequency entities.
 * 
 * @author Niraimathi
 *
 */
@Service
public class FrequencyServiceImpl implements FrequencyService {

  @Autowired
  FrequencyRepository frequencyRepository;

  /**
   * {@inheritDoc}
   */
  public Frequency addFrequency(Frequency frequency) {
    if (Objects.isNull(frequency)) {
      throw new BadRequestException(12006);
    }
    return frequencyRepository.save(frequency);
  }

  /**
   * {@inheritDoc}
   */
  // public Frequency getFrequencyByRiskLevel(String riskLevel) {
  //   Frequency frequency = frequencyRepository.findByRiskLevelIgnoreCaseAndIsDeleted(riskLevel, false);
  //   if (Objects.isNull(frequency)) {
  //     throw new SpiceValidation();
  //   }
  //   return frequency;
  // }

  /**
   * {@inheritDoc}
   */
  public Frequency getFrequencyById(long id) {
    return frequencyRepository.findByIdAndIsDeleted(id, false);
  }

  /**
   * {@inheritDoc}
   */
  public Frequency getFrequencyByFrequencyNameAndType(String name, String type) {
    Frequency frequency = frequencyRepository.findByNameIgnoreCaseAndTypeIgnoreCase(name, type);
    return frequency;
  }

  public List<Frequency> getFrequencyListByRiskLevel(String riskLevel) {
    return frequencyRepository.findAllByRiskLevel(riskLevel);
  }
}
