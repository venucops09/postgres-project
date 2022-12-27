package com.mdtlabs.coreplatform.spiceservice.patientlabtest.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.dto.spice.GetRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestResultRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestResultResponseDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientLabTest;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientLabTestResult;


public interface PatientLabTestService {
    /**
     * This method is to save a list of PatientLabTest Entities.
     *
     * @param patientLabTest PatientLabTest data
     * @return List of patientLabTestEntity
     * @author Rajkumar
     */
    List<PatientLabTest> createPatientLabTest(PatientLabTestRequestDTO patientLabTest);

    /**
     * This method is for retrieving a list of patientLabTest Entities.
     *
     * @param requestData Request Data
     * @return PatientLabTestResponseDTO Response containing list of PatientLabTest
     * @author Rajkumar
     */
    PatientLabTestResponseDTO getPatientLabTestList(GetRequestDTO requestData);

    /**
     * This method is used to remove the patientLabTest from the database.
     *
     * @param requestData request date object containing, patientLabTest id and tenant id
     * @return Boolean
     * @author Rajkumar
     */
    boolean removePatientLabTest(GetRequestDTO requestData);

    /**
     * Updates isReviewed and comments if given based on id and tenantId.
     *
     * @param requestData Request data containing patientLabTestId, patientTrackId, tenantId and review comments.
     * @return No of affected rows
     * @author Rajkumar
     */
    int reviewPatientLabTest(GetRequestDTO requestData);

    /**
     * Create patient labtest results.
     *
     * @param requestData Request Data
     * @return Created PatientLabTestResults
     * @author Rajkumar
     */
    List<PatientLabTestResult> createPatientLabTestResult(PatientLabTestResultRequestDTO requestData);

    /**
     * Get patient lab test details with result
     *
     * @param requestData Request Data
     * @return Response containing the list of patientLabTestResult.
     * @author Rajkumar
     */
    PatientLabTestResultResponseDTO getPatientLabTestResults(PatientLabTestResultRequestDTO requestData);

    List<PatientLabTest> getPatientLabTest(Long patientTrackId, Long patinetVisitId);

    int getLabtestCount(Long patientTrackId);
}
