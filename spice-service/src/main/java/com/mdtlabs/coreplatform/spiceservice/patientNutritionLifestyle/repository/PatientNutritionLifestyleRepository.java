package com.mdtlabs.coreplatform.spiceservice.patientNutritionLifestyle.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientNutritionLifestyle;

import java.util.List;

/**
 * <p>
 * This is the repository class for communication link between the server and
 * database. This class is used to perform all the Patient Nutrition Lifestyle
 * actions in database. By default, value for the annotation - nativeQuery is
 * FALSE (@nativeQuery) and acts like an HQL query. If nativeQuery is explicitly
 * mentioned TRUE, its acts like an SQL query.
 * </p>
 *
 * @author Victor Jefferson
 */
@Repository
@Transactional
public interface PatientNutritionLifestyleRepository extends JpaRepository<PatientNutritionLifestyle, Long> {

    public static final String GET_PATIENT_NUTRITION_LIFESTYLE_BY_IDS = "select patientNutritionLifestyle from PatientNutritionLifestyle as patientNutritionLifestyle where patientNutritionLifestyle.id in (:patientNutritionLifestyleIds)";
    public static final String UPDATE_VIEW_STATUS_BY_PATIENT_TRACK_ID_AND_PATIENT_VISIT_ID = "update PatientNutritionLifestyle as patientNutritionLifestyle set patientNutritionLifestyle.isViewed = true where patientNutritionLifestyle.patientTrackId =:patientTrackId and patientNutritionLifestyle.patientVisitId =:patientVisitId";

    public static final String NUTRITION_LIFESTYLE_COUNT = 
    "select count(*) from PatientNutritionLifestyle as lifestyle where lifestyle.isDeleted=false and lifestyle.assessedBy is not null and lifestyle.isViewed=false and lifestyle.patientTrackId=:patientTrackId";

    /**
     * Get list of Patient Nutrition Lifestyle by patientTracker
     *
     * @param patientTrackId
     * @return List of PatientNutritionLifestyle entity
     */
    public List<PatientNutritionLifestyle> findByPatientTrackId(long patientTrackId);

    /**
     * Update Patient Nutrition Lifestyles by Patient track id and Patient Visit id
     *
     * @param patientTrackId, patientVisitId
     * @retucom.mdtlabs.spice.patientNutritionLifestyle.repository.PatientNutritionLifestyleRepository.nutritionLifestyleReviewedCountrn boolean
     */
    @Modifying
    @Query(value = UPDATE_VIEW_STATUS_BY_PATIENT_TRACK_ID_AND_PATIENT_VISIT_ID)
    public void updateByPatientTrackIdAndPatientVisitId(@Param("patientTrackId") long patientTrackId, @Param("patientVisitId") long patientVisitId);

    /**
     * <p>
     * This method used to get Patient Nutrition Lifestyles using List of ids.
     * </p>
     *
     * @param patientNutritionLifestyleIds
     * @return List of PatientNutritionLifestyle Entity
     */
    @Query(value = GET_PATIENT_NUTRITION_LIFESTYLE_BY_IDS)
    public List<PatientNutritionLifestyle> getPatientNutritionLifestyleByIds(@Param("patientNutritionLifestyleIds") List<Long> patientNutritionLifestyleIds);

    /**
     * <p>
     * This method used to get Patient Nutrition Lifestyles review count .
     * </p>
     *
     * @param patientTrackId
     * @return int count
     */
    @Query(value = NUTRITION_LIFESTYLE_COUNT)
    public int nutritionLifestyleReviewedCount(@Param("patientTrackId") Long patientTrackId);
}
