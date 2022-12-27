package com.mdtlabs.coreplatform.spiceadminservice.labTestResultRanges.controller;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.model.dto.spice.LabTestResultRangeDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.LabTestResultRangeRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTestResultRange;

import com.mdtlabs.coreplatform.spiceadminservice.labTestResultRanges.service.LabTestResultRangesService;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessResponse;

/**
 * This controller class maintains the CRUD operations for lab test result
 * ranges.
 * 
 * @author Rajkumar
 *
 */
@RestController
@RequestMapping(value = "/labTestResultRanges")
@Validated
public class LabTestResultRangesController {

	@Autowired
	LabTestResultRangesService labTestResultRangesService;

	ModelMapper modelMapper = new ModelMapper();

	private static final List<String> noDataList = Arrays.asList(Constants.NO_DATA_FOUND);

	/**
	 * To add the lab test result ranges for the lab test result by its id
	 * 
	 * @param labTestResultRangeRequestDTO
	 * @return List of added LabTestResultRange
	 * @author Rajkumar
	 */
	@PostMapping
	public SuccessResponse<List<LabTestResultRange>> addLabTestResultRanges(@Valid @RequestBody LabTestResultRangeRequestDTO labTestResultRangeRequestDTO) {
		labTestResultRangesService.addLabTestResultRanges(labTestResultRangeRequestDTO);
		return new SuccessResponse<>(SuccessCode.LAB_TEST_RESULT_RANGE_SAVE, HttpStatus.CREATED);
	}

	/**
	 * To update the lab test result ranges
	 * 
	 * @param labTestResultRangeRequestDTO
	 * @return List of updated LabTestResultRange
	 * @author Rajkumar
	 */
	@PutMapping
	public SuccessResponse<List<LabTestResultRange>> updateLabTestResultRanges(
			@Valid @RequestBody LabTestResultRangeRequestDTO labTestResultRangeRequestDTO) {
		labTestResultRangesService.updateLabTestResultRanges(labTestResultRangeRequestDTO);
//		MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(updatedLabTestResultRanges);
//			SimpleBeanPropertyFilter simpleBeanPropertyFilter = SimpleBeanPropertyFilter.filterOutAllExcept("id",
//					"labTestId", "labTestResultId");
//			FilterProvider filterProvider = new SimpleFilterProvider().addFilter("labTestResultRangeFilter",
//					simpleBeanPropertyFilter);
//			mappingJacksonValue.setFilters(filterProvider);
//		return mappingJacksonValue;
		return new SuccessResponse<List<LabTestResultRange>>(SuccessCode.LAB_TEST_RESULT_RANGE_UPDATE, HttpStatus.OK);
	}

	/**
	 * To remove the lab test result range by its id
	 * 
	 * @param id
	 * @return boolean
	 * @author Rajkumar
	 */
	@PutMapping(value = "/{id}")
	public SuccessResponse<Boolean> removeLabTestResultRanges(@PathVariable long id) {
		labTestResultRangesService.removeLabTestResultRange(id);
		return new SuccessResponse<>(SuccessCode.LAB_TEST_RESULT_RANGE_DELETE, HttpStatus.OK);
	}

	/**
	 * To list the lab test result range's by lab test result id
	 * 
	 * @param labTestResultId
	 * @return List of LabTestResultRanges
	 * @author Rajkumar
	 */
	@GetMapping(value = "/{labTestResultId}")
	public SuccessResponse<List<LabTestResultRangeDTO>> getLabTestResultRanges(@PathVariable long labTestResultId) {
		List<LabTestResultRange> retrievedLabTestResultRanges = labTestResultRangesService
				.getLabTestResultRange(labTestResultId);
//		MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(retrievedLabTestResultRanges);
		if (!retrievedLabTestResultRanges.isEmpty()) {
//			SimpleBeanPropertyFilter simpleBeanPropertyFilter = SimpleBeanPropertyFilter.filterOutAllExcept(
//					"minimumValue", "maximumValue", "unit", "unitId", "displayOrder", "displayName");
//			FilterProvider filterProvider = new SimpleFilterProvider().addFilter("labTestResultRangeFilter",
//					simpleBeanPropertyFilter);
//			MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(retrievedLabTestResultRanges);
//			mappingJacksonValue.setFilters(filterProvider);

			List<LabTestResultRangeDTO> labTestResultRanges = modelMapper.map(retrievedLabTestResultRanges,
					new TypeToken<List<LabTestResultRangeDTO>>() {
					}.getType());

			return new SuccessResponse<List<LabTestResultRangeDTO>>(SuccessCode.GET_LAB_TEST_RESULT_RANGE,
					labTestResultRanges, labTestResultRanges.size(), HttpStatus.OK);
//			return mappingJacksonValue;
		}
		return new SuccessResponse<List<LabTestResultRangeDTO>>(SuccessCode.GET_LAB_TEST_RESULT_RANGE, noDataList, 0,
				HttpStatus.OK);
//		return mappingJacksonValue;
	}

}
