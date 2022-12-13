package com.mdtlabs.coreplatform.spiceadminservice.program.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataConflictException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.Program;
import com.mdtlabs.coreplatform.common.util.Pagination;
import com.mdtlabs.coreplatform.spiceadminservice.program.repository.ProgramRepository;
import com.mdtlabs.coreplatform.spiceadminservice.program.service.ProgramService;


/**
 * This class implements the Program interface and contains actual business
 * logic to perform operations on Program entity.
 * 
 * @author Karthick M
 *
 */
@Service
public class ProgramServiceImpl implements ProgramService {

	@Autowired
	private ProgramRepository programRepository;

    private ModelMapper mapper = new ModelMapper();


	/**
	 * {@inheritDoc}
	 */
	public Program createProgram(Program program) {
		if (Objects.isNull(program)) {
			throw new BadRequestException(12006);
		} else {
			// name should be unique in country findByNameAndIsDeleted
			Program existingProgram = programRepository.findByNameAndTenantIdAndIsDeleted(program.getName(), program.getTenantId(), false);
			if (!Objects.isNull(existingProgram)) {
				throw new DataConflictException(13002);
			}

			Program programResponse = programRepository.save(program);
			return programResponse;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Program getProgramById(long id) {
		Program program = programRepository.findByIdAndIsDeleted(id, false);
		if (Objects.isNull(program)) {
			throw new DataNotFoundException(13001);
		}
		return program;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean removeProgram(long id) {
		Program program = getProgramById(id);
		program.setDeleted(true);
		return !Objects.isNull(programRepository.save(program));
	}

	/**
	 * {@inheritDoc}
	 */
	public Program updateProgram(Program program) {
		if (Objects.isNull(program)) {
			throw new BadRequestException(12001);
		} else {
			if (!Objects.isNull(program.getName())) {  // check name from request data
				throw new DataNotAcceptableException(13004);
			}

			Program existingProgram = programRepository.findByIdAndIsDeleted(program.getId(), false);
			if (Objects.isNull(existingProgram)) {
				throw new DataNotFoundException(13001);
			}
//			to remove created_at and created_by from request data
//			delete data.created_at;
//			delete data.created_by;
			program.setCreatedAt(null);
			program.setCreatedBy(existingProgram.getCreatedBy());
			mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
			mapper.map(program, existingProgram);

			return programRepository.save(existingProgram);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Program> getAllPrograms(RequestDTO requestObject) {
		Pageable pageable = Pagination.setPagination(requestObject.getSkip(), requestObject.getLimit());

		String formattedSearchTerm = requestObject.getSearchTerm();
		if (Objects.isNull(requestObject.getSearchTerm()) && 0 < requestObject.getSearchTerm().length()) {
			formattedSearchTerm = requestObject.getSearchTerm().replaceAll("[^a-zA-Z0-9 ]*", "");	
		}

		Page<Program> programs = programRepository.getAllProgram(formattedSearchTerm,
				requestObject.getCountryId(), pageable);
		return programs.stream().collect(Collectors.toList());

	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<Program> getProgramsBySiteIds(List<Long> siteIds) {
		return programRepository.findProgramsBySiteIds(siteIds);
	}

}
