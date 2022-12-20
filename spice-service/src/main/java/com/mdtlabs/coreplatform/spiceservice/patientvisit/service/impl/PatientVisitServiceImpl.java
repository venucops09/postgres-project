package com.mdtlabs.coreplatform.spiceservice.patientvisit.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.contexts.UserContextHolder;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.model.dto.TimezoneDTO;
import com.mdtlabs.coreplatform.common.model.dto.UserDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.CommonRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientVisit;
import com.mdtlabs.coreplatform.common.util.CommonUtil;
import com.mdtlabs.coreplatform.common.util.DateUtil;
import com.mdtlabs.coreplatform.spiceservice.patientvisit.repository.PatientVisitRepository;
import com.mdtlabs.coreplatform.spiceservice.patientvisit.service.PatientVisitService;

/**
 * This class implements the PatientVisitService interface and contains actual
 * business logic to perform operations on PatientVisit entity.
 *
 * @author Karthick Murugesan
 */
@Service
public class PatientVisitServiceImpl implements PatientVisitService {

	@Autowired
	private PatientVisitRepository patientVisitRepository;

	
	public Map<String, Long> addPatientVisit(CommonRequestDTO patientVisitDTO) {
        UserDTO userDto = UserContextHolder.getUserDto();
        TimezoneDTO timeZone = userDto.getTimezone();
        String startDate = DateUtil.getStartOfDay(timeZone.getOffset());
        String endDate = DateUtil.getEndOfDay(timeZone.getOffset());
		PatientVisit existingPatientVisit = patientVisitRepository
				.getPatientVisitByTrackId(patientVisitDTO.getPatientTrackId(), startDate, endDate);
		PatientVisit patientVisit;
		if (Objects.isNull(existingPatientVisit)) {
			patientVisit = new PatientVisit();
			patientVisit.setTenantId(patientVisitDTO.getTenantId());
			patientVisit.setPatientTrackId(patientVisitDTO.getPatientTrackId());
			patientVisit.setVisitDate(new Date());
			patientVisit = patientVisitRepository.save(patientVisit);
		} else {
			patientVisit = existingPatientVisit;
		}
		return Map.of("id", patientVisit.getId());
	}

	public PatientVisit updatePatientVisit(PatientVisit patientVisit) {
		if (Objects.isNull(patientVisit)) {
			throw new BadRequestException(1000);
		}
		return patientVisitRepository.save(patientVisit);
	}

	public PatientVisit getPatientVisit(Long id, Long tenantId) {
		return patientVisitRepository.findByIdAndTenantId(id, tenantId);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PatientVisit> getPatientVisitDates(Long patientTrackId, Boolean isInvestigation,
			Boolean isMedicalReview, Boolean isPrescription) {
		List<PatientVisit> visitDates = patientVisitRepository.getPatientVisitDates(patientTrackId, isInvestigation,
				isMedicalReview, isPrescription);
		return Objects.isNull(visitDates) ? new ArrayList<>() : visitDates;
	}

	@Override
	public PatientVisit getPatientVisitById(Long id) {
		return patientVisitRepository.findByIdAndIsDeleted(id, Constants.BOOLEAN_FALSE);

	}

}
