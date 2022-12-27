package com.mdtlabs.coreplatform.spiceservice.patientlabtest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.dto.spice.GetRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PatientLabTestResultRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientLabTest;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientLabTestResult;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.spiceservice.patientlabtest.service.PatientLabTestService;

import javax.validation.*;
import java.util.List;

/**
 * This class is the controller for the PatientLabTest Entity. This class is responsible for receiving the api request
 * and for sending the request to the corresponding methods.
 *
 * @author Rajkumar
 */
@Validated
@RequestMapping(value = "/patient-labtest")
@RestController
public class PatientLabTestController {
    @Autowired
    private PatientLabTestService patientLabTestService;

    /**
     * This method is to save a list of PatientLabTest Entities
     *
     * @param requestData Request Data
     * @return List of PatientLabTest entities.
     * @author Rajkumar
     */
    @RequestMapping(method = RequestMethod.POST)
    public SuccessResponse<PatientLabTest> createPatientLabTest(@Valid @RequestBody PatientLabTestRequestDTO requestData) {
        patientLabTestService.createPatientLabTest(requestData);
        return new SuccessResponse<>(SuccessCode.PATIENT_LABTEST_SAVE, HttpStatus.CREATED);
    }

    /**
     * This method is for retrieving a list of patientLabTest Entities.
     *
     * @param requestData Request Data
     * @return List of PatientLabTest entity.
     * @author Rajkumar
     */
    @RequestMapping(method = RequestMethod.GET)
    public SuccessResponse<PatientLabTestResponseDTO> getPatientLabTestList(@RequestBody GetRequestDTO requestData) {
        return new SuccessResponse<>(SuccessCode.GET_PATIENT_lABTEST_LIST,
                patientLabTestService.getPatientLabTestList(requestData), HttpStatus.OK);
    }

    /**
     * Removes the patient labtest based on id.
     *
     * @param requestData Request Data containing id.
     * @return Boolean
     * @author Rajkumar
     */
    @RequestMapping(method = RequestMethod.DELETE)
    public SuccessResponse<String> removePatientLabTest(@RequestBody GetRequestDTO requestData) {
        patientLabTestService.removePatientLabTest(requestData);
        return new SuccessResponse<>(SuccessCode.PATIENT_LABTEST_DELETE, HttpStatus.OK);
    }

    /**
     * Review patient labtest details.
     *
     * @param requestData RequestData
     * @return SuccessResponse containing success message
     * @author Rajkumar
     */
    @RequestMapping(value = "/review", method = RequestMethod.GET)
    public SuccessResponse<String> reviewPatientLabTest(@RequestBody GetRequestDTO requestData) {
        patientLabTestService.reviewPatientLabTest(requestData);
        return new SuccessResponse<>(SuccessCode.REVIEW_PATIENT_LABTEST, HttpStatus.OK);
    }

    /**
     * Create a new patientLabTestResults.
     *
     * @param requestData Request Data
     * @return SuccessResponse containing success message
     * @author Rajkumar
     */
    @RequestMapping(value = "/result/create", method = RequestMethod.POST)
    public SuccessResponse<List<PatientLabTestResult>> createPatientLabTestResult(
            @Valid @RequestBody PatientLabTestResultRequestDTO requestData) {
        patientLabTestService.createPatientLabTestResult(requestData);
        return new SuccessResponse<List<PatientLabTestResult>>(SuccessCode.PATIENT_LABTEST_RESULT_SAVE, HttpStatus.CREATED);
    }

    /**
     * Gets PatientLabTestResult List.
     *
     * @param requestData Request data
     * @return SuccessResponse containing success message
     * @author Rajkumar
     */
    @RequestMapping(value = "/result/details", method = RequestMethod.GET)
    public SuccessResponse<String> getPatientLabTestResults(@RequestBody PatientLabTestResultRequestDTO requestData) {
        return new SuccessResponse<>(SuccessCode.GOT_PATIENT_LABTEST_RESULTS,
                patientLabTestService.getPatientLabTestResults(requestData), HttpStatus.OK);
    }
}