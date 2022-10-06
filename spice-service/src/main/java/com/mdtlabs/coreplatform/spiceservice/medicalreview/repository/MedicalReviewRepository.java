package com.mdtlabs.coreplatform.spiceservice.medicalreview.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.PatientMedicalReview;


/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the PatientMedicalReview module action in database.
 * In query annotation (nativeQuery = true) the below query perform like SQL.
 * Otherwise its perform like HQL default value for nativeQuery FALSE
 * </p>
 * 
 * @author Karthick Murugesan
 */
@Repository
public interface MedicalReviewRepository extends JpaRepository<PatientMedicalReview, Long> {

    public static final String GET_PATIENT_MEDICAL_REVIEW = 
    "from PatientMedicalReview as medicalreview where medicalreview.patientTrackId=:patientTrackId and " +
    "medicalreview.isDeleted=false and (:patientVisitId is null or medicalreview.patientVisitId=:patientVisitId)";

    /**
     * Gets medical review for a patient based on patinetTrackId and patientVisitId
     * 
     * @param patientTrackId Long 
     * @param patientVisitId Long
     * @return List of patientMedicalReview
     */
    @Query(value = GET_PATIENT_MEDICAL_REVIEW)
    public List<PatientMedicalReview> getPatientMedicalReview(@Param("patientTrackId") Long patientTrackId, @Param("patientVisitId") Long patientVisitId);

    
}
