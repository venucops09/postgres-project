package com.mdtlabs.coreplatform.spiceadminservice.program.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.Program;

import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceadminservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceadminservice.program.service.ProgramService;

/**
 * This class is a controller class to perform operation on Program entity.
 * 
 * @author Rajkumar
 * 
 */
@RestController
@RequestMapping(value = "/program")
@Validated
public class ProgramController {

	@Autowired
	ProgramService programService;

	/**
	 * This method is used to add a new program.
	 * 
	 * @param program request data containing program details
	 * @return program Entity.
	 * @author Rajkumar
	 */
	@RequestMapping(method = RequestMethod.POST)
	public SuccessResponse<Program> createProgram(@Valid @RequestBody Program program) {
		programService.createProgram(program);
		return new SuccessResponse<>(SuccessCode.PROGRAM_SAVE, HttpStatus.CREATED);
	}

	/**
	 * This method is used to retrieve single program details using programId
	 * 
	 * @param programId program id to get program entity.
	 * @return Program Entity
	 * @author Rajkumar
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{id}")
	public SuccessResponse<Program> getProgramById(@PathVariable(value = "id") long programId) {
		return new SuccessResponse<Program>(SuccessCode.GET_PROGRAM, programService.getProgramById(programId),
				HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve single program details using programId
	 * 
	 * @param request request data
	 * @return Program Entity
	 * @author Rajkumar
	 */
	@RequestMapping(method = RequestMethod.GET)
	public SuccessResponse<Program> getPrograms(@RequestBody RequestDTO request) {
		return new SuccessResponse<Program>(SuccessCode.GET_PROGRAM, programService.getAllPrograms(request),
				HttpStatus.OK);
	}

	/**
	 * Used to soft delete a program.
	 * 
	 * @param programId
	 * @return Boolean
	 * @author Rajkumar
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public SuccessResponse<Boolean> removeProgram(@PathVariable(value = "id") long programId) {
		programService.removeProgram(programId);
		return new SuccessResponse<Boolean>(SuccessCode.PROGRAM_DELETE, HttpStatus.OK);
	}

	/**
	 * Used to update a program detail like name, etc.,
	 * 
	 * @param program
	 * @return program Entity
	 * @author Rajkumar
	 */
	@RequestMapping(method = RequestMethod.PATCH)
	public SuccessResponse<Program> updateProgram(@RequestBody Program program) {
		programService.updateProgram(program);
		return new SuccessResponse<>(SuccessCode.PROGRAM_STATUS_UPDATE, HttpStatus.OK);
	}
	
    /**
     * Gets list of programs using list of site Ids
     * 
     * @param siteIds List of siteIds
     * @return List of Program Entities
     */
	@PostMapping("/get-by-site-ids")
	public List<Program> getProgramsBySiteIds(@RequestBody List<Long> siteIds) {
		return programService.getProgramsBySiteIds(siteIds);
	}

}
