package com.mdtlabs.coreplatform.spiceservice.mentalhealth.service.impl;

import java.util.Objects;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.dto.spice.MentalHealthDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.MentalHealth;
import com.mdtlabs.coreplatform.common.model.entity.spice.MentalHealthDetails;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.spiceservice.mentalhealth.repository.MentalHealthRepository;
import com.mdtlabs.coreplatform.spiceservice.mentalhealth.service.MentalHealthService;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;


/**
 * This class implements the MentalHealthService class and contains business
 * logic for the operations of MentalHealth Entity.
 * 
 * @author Karthick Murugesan
 */
@Service
public class MentalHealthServiceImpl implements MentalHealthService {

	@Autowired
	MentalHealthRepository mentalHealthRepository;

	@Autowired
	PatientTrackerService patientTrackerService;

	private ModelMapper mapper = new ModelMapper();

	/**
	 * {@inheritDoc}
	 */
	public MentalHealth createOrUpdateMentalHealth(MentalHealth mentalHealth) {
		MentalHealth newMentalHealth;
		if (Objects.isNull(mentalHealth.getPatientTrackId())) {
			throw new BadRequestException(10010);
		}
		PatientTracker patientTracker = patientTrackerService.getPatientTrackerById(mentalHealth.getPatientTrackId());
		setPHQ4Score(mentalHealth);
		mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		if (Objects.isNull(mentalHealth.getId())) {
			MentalHealth oldMentalHealth = mentalHealthRepository
					.findByPatientTrackIdAndIsDeletedAndIsLatest(mentalHealth.getPatientTrackId(), false, true);
			if (!Objects.isNull(oldMentalHealth)) {
				// delete data.created_at;
				// delete data.created_by;
				mapper.map(mentalHealth, oldMentalHealth);
				newMentalHealth = mentalHealthRepository.save(oldMentalHealth);
			} else {
				newMentalHealth = createMentalHealth(mentalHealth);
				// mentalHealthRepository.updateLatestStatus(mentalHealth.getPatientTrackId());
				// mentalHealth.setIsLatest(Constants.BOOLEAN_TRUE);
				// newMentalHealth = mentalHealthRepository.save(mentalHealth);
			}
		} else {
			MentalHealth existingMentalHealth = mentalHealthRepository.findByIdAndIsDeleted(mentalHealth.getId(),
					false);
			if (Objects.isNull(existingMentalHealth)) {
				throw new DataNotFoundException(2001);
			}
			// delete data.created_at;
			// delete data.created_by;
			mapper.map(mentalHealth, existingMentalHealth);
			newMentalHealth = mentalHealthRepository.save(existingMentalHealth);

		}
		updatePatientTracker(patientTracker, mentalHealth);
		return newMentalHealth;
	}

	/**
	 * Update the mental health details in patientTracker
	 * 
	 * @param patientTracker
	 * @param mentalHealth
	 * @author Karthick Murugesan
	 */
	private void updatePatientTracker(PatientTracker patientTracker, MentalHealth mentalHealth) {
		patientTracker.setTenantId(mentalHealth.getTenantId());
		if (!Objects.isNull(mentalHealth.getPhq4MentalHealth()) && !mentalHealth.getPhq4MentalHealth().isEmpty()) {
			patientTracker.setPhq4FirstScore(mentalHealth.getPhq4FirstScore());
			patientTracker.setPhq4SecondScore(mentalHealth.getPhq4SecondScore());
			patientTracker.setPhq4Score(mentalHealth.getPhq4Score());
			patientTracker.setPhq4RiskLevel(mentalHealth.getPhq4RiskLevel());
		}
		if (!Objects.isNull(mentalHealth.getPhq9MentalHealth()) && !mentalHealth.getPhq9MentalHealth().isEmpty()) {
			patientTracker.setPhq9Score(mentalHealth.getPhq9Score());
			patientTracker.setPhq9RiskLevel(mentalHealth.getPhq9RiskLevel());
		}
		if (!Objects.isNull(mentalHealth.getGad7MentalHealth()) && !mentalHealth.getGad7MentalHealth().isEmpty()) {
			patientTracker.setGad7Score(mentalHealth.getGad7Score());
			patientTracker.setGad7RiskLevel(mentalHealth.getGad7RiskLevel());
		}
		patientTrackerService.addOrUpdatePatientTracker(patientTracker);
	}

	/**
	 * Sets the phq4 first and second scores
	 * 
	 * @param mentalHealth
	 * @author Karthick Murugesan
	 */
	public void setPHQ4Score(MentalHealth mentalHealth) {
		mentalHealth.setPhq4FirstScore(Constants.ZERO);
		mentalHealth.setPhq4SecondScore(Constants.ZERO);
		if (!Objects.isNull(mentalHealth)) {
			for (MentalHealthDetails mentalHealthDetails : mentalHealth.getPhq4MentalHealth()) {
				if (mentalHealthDetails.getDisplayOrder() == Constants.ONE
						|| mentalHealthDetails.getDisplayOrder() == Constants.TWO) {
					mentalHealth.setPhq4FirstScore(mentalHealth.getPhq4FirstScore() + mentalHealthDetails.getScore());
				} else if (mentalHealthDetails.getDisplayOrder() == Constants.THREE
						|| mentalHealthDetails.getDisplayOrder() == Constants.FOUR) {
					mentalHealth.setPhq4SecondScore(mentalHealth.getPhq4SecondScore() + mentalHealthDetails.getScore());
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public MentalHealthDTO getMentalHealthDetails(RequestDTO requestData) {
		if (Objects.isNull(requestData.getPatientTrackId())) {
			throw new DataNotAcceptableException(10010);
		}
		MentalHealth mentalHealth = mentalHealthRepository
				.findByPatientTrackIdAndIsDeletedAndIsLatest(requestData.getPatientTrackId(), false, true);
		MentalHealthDTO mentalHealthDTO = new MentalHealthDTO();
		// TypeMap<MentalHealth,MentalHealthDTO> typeMap =
		// modelMapper.createTypeMap(MentalHealth.class, MentalHealthDTO.class);
		// typeMap.addMappings(modelMapper ->
		// modelMapper.skip(MentalHealthDTO::setGad7MentalHealth));
		mentalHealthDTO.setId(mentalHealth.getId());
		if (requestData.getType().equals(Constants.PHQ4)) {
			mentalHealthDTO.setPhq4MentalHealth(mentalHealth.getPhq4MentalHealth());
			mentalHealthDTO.setPhq4RiskLevel(mentalHealth.getPhq4RiskLevel());
			mentalHealthDTO.setPhq4Score(mentalHealth.getPhq4Score());
		}
		if (requestData.getType().equals(Constants.PHQ9)) {
			mentalHealthDTO.setPhq9MentalHealth(mentalHealth.getPhq9MentalHealth());
			mentalHealthDTO.setPhq9RiskLevel(mentalHealth.getPhq9RiskLevel());
			mentalHealthDTO.setPhq9Score(mentalHealth.getPhq9Score());
		}
		if (requestData.getType().equals(Constants.GAD7)) {
			mentalHealthDTO.setGad7MentalHealth(mentalHealth.getGad7MentalHealth());
			mentalHealthDTO.setGad7RiskLevel(mentalHealth.getGad7RiskLevel());
			mentalHealthDTO.setGad7Score(mentalHealth.getGad7Score());
		}

		return mentalHealthDTO;
	}

    public MentalHealth createMentalHealth(MentalHealth mentalHealth) {
        mentalHealthRepository.updateLatestStatus(mentalHealth.getPatientTrackId());
        mentalHealth.setLatest(Constants.BOOLEAN_TRUE);
        return mentalHealthRepository.save(mentalHealth);
    }
    
}
