package com.mdtlabs.coreplatform.spiceadminservice.labTestResultRanges.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.logger.SpiceLogger;
import com.mdtlabs.coreplatform.common.model.dto.spice.LabTestResultRangeDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.LabTestResultRangeRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTestResult;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTestResultRange;
import com.mdtlabs.coreplatform.spiceadminservice.labTest.repository.LabTestResultRepository;
import com.mdtlabs.coreplatform.spiceadminservice.labTestResultRanges.repository.LabTestResultRangesRepository;
import com.mdtlabs.coreplatform.spiceadminservice.labTestResultRanges.service.LabTestResultRangesService;


/**
 * This service maintains the CRUD operations for Lab test result range
 *
 * @author Rajkumar
 */
@Service
public class LabTestResultRangesServiceImpl implements LabTestResultRangesService {

	@Autowired
	private LabTestResultRangesRepository repository;

	@Autowired
	private LabTestResultRepository resultRepository;

	ModelMapper modelMapper = new ModelMapper();

	/**
	 * {@inheritDoc}
	 */
	public List<LabTestResultRange> addLabTestResultRanges(LabTestResultRangeRequestDTO labTestResultRangeRequestDTO) {

		if (Objects.isNull(labTestResultRangeRequestDTO)
				|| labTestResultRangeRequestDTO.getLabTestResultRanges().isEmpty()) {
			throw new BadRequestException(12006);
		}

		if (Objects.isNull(labTestResultRangeRequestDTO.getLabTestResultId())) {
			throw new BadRequestException(28008);
		}

		LabTestResult labTestResult = resultRepository
				.findByIdAndIsDeleted(labTestResultRangeRequestDTO.getLabTestResultId(), false);

		if (Objects.isNull(labTestResult)) {
			throw new DataNotAcceptableException(28010);
		}

		List<LabTestResultRange> listOfLabTestResultRangesToSave = new ArrayList<>();

		List<LabTestResultRangeDTO> labTestResultRanges = labTestResultRangeRequestDTO.getLabTestResultRanges();
		SpiceLogger.logInfo("labTestResultRanges: " + labTestResultRanges);
		for (int i = 0; i < labTestResultRanges.size(); i++) {
			LabTestResultRangeDTO labTestResultRangeDTO = labTestResultRanges.get(i);
			LabTestResultRange labTestResultRangeEntity = new LabTestResultRange();
			labTestResultRangeEntity.setTenantId(labTestResultRangeRequestDTO.getTenantId());
			labTestResultRangeEntity.setLabTestId(labTestResult.getLabTestId());
			labTestResultRangeEntity.setLabTestResultId(labTestResult.getId());
			if (!Objects.isNull(labTestResultRangeDTO.getMinimumValue())) {
				labTestResultRangeEntity.setMinimumValue(labTestResultRangeDTO.getMinimumValue());
			}
			if (!Objects.isNull(labTestResultRangeDTO.getMaximumValue())) {
				labTestResultRangeEntity.setMaximumValue(labTestResultRangeDTO.getMaximumValue());
			}
			if (!Objects.isNull(labTestResultRangeDTO.getDisplayName())) {
				labTestResultRangeEntity.setDisplayName(labTestResultRangeDTO.getDisplayName());
			}
			if (!Objects.isNull(labTestResultRangeDTO.getDisplayOrder())) {
				labTestResultRangeEntity.setDisplayOrder(labTestResultRangeDTO.getDisplayOrder());
			}
			if (!Objects.isNull(labTestResultRangeDTO.getUnit())) {
				labTestResultRangeEntity.setUnit(labTestResultRangeDTO.getUnit());
			}
			if (!Objects.isNull(labTestResultRangeDTO.getUnitId())) {
				labTestResultRangeEntity.setUnitId(labTestResultRangeDTO.getUnitId());
			}
			listOfLabTestResultRangesToSave.add(labTestResultRangeEntity);
		}
		return repository.saveAll(listOfLabTestResultRangesToSave);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<LabTestResultRange> updateLabTestResultRanges(
			LabTestResultRangeRequestDTO labTestResultRangeRequestDTO) {

		if (Objects.isNull(labTestResultRangeRequestDTO)) {
			throw new BadRequestException(12006);
		}

		SpiceLogger.logInfo(
				"Updating lab test result ranges for result id: " + labTestResultRangeRequestDTO.getLabTestResultId());

		List<LabTestResultRangeDTO> labTestResultRangeList = labTestResultRangeRequestDTO.getLabTestResultRanges();

		if (Objects.isNull(labTestResultRangeList) || labTestResultRangeList.isEmpty()) {
			throw new BadRequestException(12006);
		}

		List<Long> labTestResultRangeIds = labTestResultRangeList.stream()
				.map(labTestResultRange -> labTestResultRange.getId()).collect(Collectors.toList());

		List<LabTestResultRange> existingLabTestResultRangesList = repository
				.findByIdsAndIsDeleted(labTestResultRangeIds, false);

		List<LabTestResultRange> labTestResultRangesToUpdate = new ArrayList<>();

		if (Objects.isNull(existingLabTestResultRangesList) || existingLabTestResultRangesList.isEmpty()
				|| existingLabTestResultRangesList.size() != labTestResultRangeList.size()) {
			throw new DataNotFoundException(28006);
		}

		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
		long labTestId = 0;
		long labTestResultId = 0;
		for (int i = 0; i < labTestResultRangeList.size(); i++) {
			LabTestResultRangeDTO labTestResultRangeDTO = labTestResultRangeList.get(i);
			LabTestResultRange existingLabTestResultRangeEntity = existingLabTestResultRangesList.stream().filter(
					existingLabTestResultRange -> (existingLabTestResultRange.getId() == labTestResultRangeDTO.getId()))
					.findFirst().orElseThrow(() -> new DataNotFoundException(28006));
			if (i == 0) {
				labTestId = existingLabTestResultRangeEntity.getLabTestId();
				labTestResultId = existingLabTestResultRangeEntity.getLabTestResultId();
			}
			modelMapper.map(labTestResultRangeDTO, existingLabTestResultRangeEntity);
			existingLabTestResultRangeEntity.setLabTestId(labTestId);
			existingLabTestResultRangeEntity.setLabTestResultId(labTestResultId);
			existingLabTestResultRangeEntity.setId(labTestResultRangeDTO.getId());
			labTestResultRangesToUpdate.add(existingLabTestResultRangeEntity);
		}

		return repository.saveAll(labTestResultRangesToUpdate);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeLabTestResultRange(long id) {
		repository.removeLabTestResultRange(id, true);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<LabTestResultRange> getLabTestResultRange(long labTestResultId) {

		List<LabTestResultRange> labTestResultRangeList = repository.findByLabTestResultIdAndIsDeleted(labTestResultId,
				false);
//		LabTestResultRangeDTO labTestResultRangeDTO = null;
//		List<LabTestResultRangeDTO> labTestRestResultRangeDTOs = new ArrayList<>();
//		for (LabTestResultRange labTestResultRange : labTestResultRangeList) {
//			labTestResultRangeDTO = new LabTestResultRangeDTO();
//			modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
//			modelMapper.map(labTestResultRange, labTestResultRangeDTO);
//			labTestRestResultRangeDTOs.add(labTestResultRangeDTO);
//		}
//		if (Objects.isNull(labTestRestResultRangeDTOs)) {
//			throw new SpiceValidation(28009);
//		}

//		return labTestRestResultRangeDTOs;

		return labTestResultRangeList;
	}
}
