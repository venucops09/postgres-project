package com.mdtlabs.coreplatform.spiceadminservice.program.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.Program;


/**
 * This is an interface to perform any actions in Program entities
 *
 * @author Karthick M
 */
public interface ProgramService {

    /**
     * Creates a new program
     *
     * @param program
     * @return Program
     */
    Program createProgram(Program program);

    /**
     * Gets a program details based on id
     *
     * @param id
     * @return Program
     */
    Program getProgramById(long id);

    /**
     * Updates the program
     *
     * @param program
     * @return Program
     */
    Program updateProgram(Program program);

    /**
     * Soft delete the program
     *
     * @param id
     * @return boolean
     */
    boolean removeProgram(long id);

    /**
     * Gets all the program based on request object
     *
     * @param requestObject
     * @return List<Program>
     */
    List<Program> getAllPrograms(RequestDTO requestObject);

    
    /**
     * Gets list of programs using list of site Ids
     * 
     * @param siteIds List of siteIds
     * @return List of Program Entities
     */
	List<Program> getProgramsBySiteIds(List<Long> siteIds);
}
