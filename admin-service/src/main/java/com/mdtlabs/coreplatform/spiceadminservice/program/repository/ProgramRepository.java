package com.mdtlabs.coreplatform.spiceadminservice.program.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.Program;

/**
 * This class is a repository class to establish communication between database
 * and server side.
 * 
 * @author Karthick M
 *
 */
@Repository
public interface ProgramRepository extends JpaRepository<Program, Long>{

    public static final String GET_ALL_PROGRAMS =
    "select program from Program as program where (:countryId is null or program.countryId=:countryId)" +
    " and program.isDeleted=false and (:searchTerm is null or lower(program.name) LIKE CONCAT('%',lower(:searchTerm),'%')) order by program.updatedAt DESC";


    public static final String GET_PROGRAM_BY_SITE_IDS = "select program from Program program join program.sites as site where site.id in (:siteIds)";
    /**
     * Finds the program based on its name and isDeleted
     * 
     * @param name
     * @param isDeleted
     * @return Program Entity
     */
    public Program findByNameAndTenantIdAndIsDeleted(String name,long tenantId, boolean isDeleted);

    /**
     * Finds the program based on its id and isDeleted
     * 
     * @param id
     * @param isDeleted
     * @return Program Entity
     */
    public Program findByIdAndIsDeleted(long id, boolean isDeleted);


    /**
     * Finds the program based on searchTerm and its countryId and operatingUnitId and
     * accountId and siteId 
     * 
     * @param searchTerm
     * @param countryId
     * @param operatingUnitId
     * @param accountId
     * @param siteId
     * @return Program Entity
     */
    @Query(value = GET_ALL_PROGRAMS)
    public Page<Program> getAllProgram(@Param("searchTerm") String searchTerm, @Param("countryId") Long countryId, Pageable pageable);

    /**
     * Finds the program based on searchTerm and its countryId and operatingUnitId and
     * accountId and siteId 
     * 
     * @param searchTerm
     * @param countryId
     * @param operatingUnitId
     * @param accountId
     * @param siteId
     * @param pageable
     * @return Program Entity
     */
    @Query(value = GET_ALL_PROGRAMS)
    public Page<Program> searchPrograms(@Param("searchTerm") String searchTerm, @Param("countryId") Long countryId, Pageable pageable);

    /**
     * Gets list of programs using list of site Ids
     * 
     * @param siteIds List of siteIds
     * @return List of Program Entities
     */
    @Query(value = GET_PROGRAM_BY_SITE_IDS)
	public List<Program> findProgramsBySiteIds(@Param("siteIds") List<Long> siteIds);                     
    
}
