package com.mdtlabs.coreplatform.spiceservice.patientlabtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientLabTest;

import java.util.List;

/**
 * This repository class is responsible for communication between database and server side.
 *
 * @author Rajkumar
 */
@Repository
public interface PatientLabTestRepository extends JpaRepository<PatientLabTest, Long> {

    public static final String GET_PATIENT_LABTEST_LIST = "SELECT p from PatientLabTest as p where "
            + "p.patientTrackId = :patientTrackId AND (:patientVisitId is null or p.patientVisitId = :patientVisitId) "
            + "AND p.isDeleted=:isDeleted";

    public static final String GET_PATIENT_LABTEST_LIST_WITH_CONDITION = "SELECT p from PatientLabTest as p where "
            + "p.patientTrackId = :patientTrackId AND (:patientVisitId is null or p.patientVisitId = :patientVisitId) "
            + "AND p.isDeleted=:isDeleted AND p.resultUpdateBy is null";
            

        
    public static final String GET_PATIENT_LAB_TEST_COUNT = 
    "Select count(*) from PatientLabTest as labtest where labtest.isReviewed=false and labtest.isDeleted=false and labtest.resultUpdateBy is not null and labtest.patientTrackId=:patientTrackId"; 

    public static final String UPDATE_IS_REVIEWED = "UPDATE PatientLabTest as p set p.isReviewed = true, p.comments = "
            + "COALESCE(:comments,p.comments) where p.id = :id AND (p.tenantId is null or p.tenantId= :tenantId)";


    public static final String REMOVE_PATIENT_LABTEST = "UPDATE PatientLabTest as p set p.isDeleted = true where "
            + "p.id = :id";

    public static final String GET_PATIENT_LABTEST_WITHOUT_RESULTS = "SELECT labtests from PatientLabTest as labtests "
            + "where labtests.resultDate is null AND labtests.patientTrackId =:patientTrackId AND labtests.tenantId"
            + "=:tenantId AND labtests.isDeleted = false";

    /**
     * Gets patient labTest list based on patient track id and patient visit id.
     *
     * @param patientTrackId Patient Track Id
     * @param patientVisitId Patient visit id
     * @param isDeleted
     * @return List of PatientLabTest Entities
     * @author Rajkumar
     */
    @Query(value = GET_PATIENT_LABTEST_LIST)
    List<PatientLabTest> getPatientLabTestList(@Param("patientTrackId") Long patientTrackId,
                                               @Param("patientVisitId") Long patientVisitId,
                                               @Param("isDeleted") Boolean isDeleted);


    /**
     * Gets the patient labtest list based on patientTrackId, patientVisitId and referredBy.
     *
     * @param patientTrackId Patient Track Id
     * @param patientVisitId Patient visit id
     * @param isDeleted
     * @return List of PatientLabTest Entities
     * @author Rajkumar
     */
    @Query(value = GET_PATIENT_LABTEST_LIST_WITH_CONDITION)
    List<PatientLabTest> getPatientLabTestListWithCondition(@Param("patientTrackId") Long patientTrackId,
                                                            @Param("patientVisitId") Long patientVisitId,
                                                            @Param("isDeleted") Boolean isDeleted);


    /**
     * Gets a patientLabTest based on id and isdeleted values.
     *
     * @param id        patientLabtestId
     * @param isDeleted isDeleted
     * @return PatientLabTest entity.
     * @author Rajkumar
     */
    PatientLabTest findByIdAndIsDeleted(Long id, Boolean isDeleted);

    /**
     * @param patientVisitId patient visit id.
     * @param isDeleted      isDeleted
     * @return List of PatientLabTest entities.
     * @author Rajkumar
     */
    List<PatientLabTest> findAllByPatientVisitIdAndIsDeleted(Long patientVisitId, Boolean isDeleted);

    /**
     * Updates isReviewed and comments if given based on id and tenantId.
     *
     * @param id       PatientLabTestId
     * @param tenantId tenant id
     * @param comments review comments
     * @return Number of affected rows
     * @author Rajkumar
     */
    @Transactional
    @Modifying
    @Query(UPDATE_IS_REVIEWED)
    int updateIsReviewed(@Param("id") Long id, @Param("tenantId") Long tenantId, @Param("comments") String comments);

    /**
     * Removes a patientLabTest from database.
     *
     * @param id patientLabTest
     * @return No of affected rows
     * @author Rajkumar
     */
    @Modifying
    @Transactional
    @Query(REMOVE_PATIENT_LABTEST)
    int removePatientLabTest(@Param("id") Long id);

    /**
     * Gets patient lebtest which are without result dates.
     *
     * @param patientTrackId PatientTrackId
     * @param tenantId       TenantId
     * @return List of PatientLabTest entities.
     * @author Rajkumar
     */
    @Query(GET_PATIENT_LABTEST_WITHOUT_RESULTS)
    List<PatientLabTest> getPatientLabTestsWithoutResults(@Param("patientTrackId") long patientTrackId,
                                                          @Param("tenantId") Long tenantId);

    @Query(value = GET_PATIENT_LAB_TEST_COUNT)
    public int getLabTestNoReviewedCount(@Param("patientTrackId") Long patientTrackId);


}
