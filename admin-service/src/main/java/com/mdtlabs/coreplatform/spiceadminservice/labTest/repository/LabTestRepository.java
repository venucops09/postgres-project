package com.mdtlabs.coreplatform.spiceadminservice.labTest.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.LabTest;


/**
 * 
 * This class is a repository class to establish communication between database
 * and server side.
 * 
 * @author Niraimathi S
 *
 */
@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {

    public static final String GET_ALL_LABTESTS = "select labtest from LabTest as labtest where "
            + "(:countryId is null or labtest.countryId=:countryId) AND labtest.isDeleted=false and labtest.tenantId=:tenantId "
            + " and (:searchTerm is null or lower(labtest.name) LIKE CONCAT('%',lower(:searchTerm),'%'))";

    public static final String GET_LABTEST_BY_MANDATORY_FIELDS = "select labtest from LabTest as labtest where"
            + " labtest.countryId=:country AND labtest.name=:name AND labtest.isDeleted=false order by labtest.updatedAt desc";

	public static final String GET_LABTEST_BY_NAME = "select labtest from LabTest as labtest where lower(labtest.name)"
           + " LIKE CONCAT('%',lower(:searchTerm),'%') AND labtest.countryId=:countryId AND labtest.isDeleted=false and (:isActive is null or labtest.isActive=:isActive)"
           + " and lower(labtest.name) NOT LIKE 'other' ";

    /**
     * This method retrives a single Labtest details using mandatory fields.
     * 
     * @param countryId country id
     * @param name name of the LabTest Entity.
     * @return LabTest entity
     * @author Niraimathi S
     */
    @Query(value = GET_LABTEST_BY_MANDATORY_FIELDS)
    public LabTest getByFields(@Param("country") long countryId, @Param("name") String name);

    /**
     * Used to retrieve all the labtests in a country.
     * 
     * @param countryId
     * @param pageable
     * @return List of LabTest entities.
     * @author Niraimathi S
     */
    @Query(value = GET_ALL_LABTESTS)
    public Page<LabTest> getAllLabTests(@Param("searchTerm") String searchTerm, @Param("countryId")Long countryId,@Param("tenantId")Long tenantId, Pageable pageable);


    /**
     * This method is used to get labtest based on the given search name in a country.
     * 
     * @param searchTerm
     * @param countryId
     * @return List of LabTest Entities
     * @author Niraimathi S
     */
    @Query(value = GET_LABTEST_BY_NAME)
    public List<LabTest> searchLabTests(@Param("searchTerm") String searchTerm, @Param("countryId") Long countryId, @Param("isActive") Boolean isActive);

    /**
     * This method retrives a single Labtest details using mandatory fields.
     * 
     * @return LabTest Entity
     * @author Karthick M
     */
    public LabTest findByCountryIdAndNameAndIsDeleted(long countryId, String name, boolean isDeleted);

    /**
     * This method retrives a single Labtest details.
     * 
     * @param labTestId
     * @return LabTest Entity
     * @author Karthick M
     */
    public LabTest findByIdAndIsDeleted(Long labTestId, Boolean isDeleted);

    /**
     * Returns LabTest based on name and country id and returns a single labtest.
     * 
     * @param searchTerm
     * @param countryId
     * @return Labtest Entity
     * @author Niraimathi S
     */
    public LabTest findByNameIgnoreCaseAndCountryId(String searchTerm, long countryId);

}
