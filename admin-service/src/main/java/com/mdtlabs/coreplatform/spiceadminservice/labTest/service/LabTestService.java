package com.mdtlabs.coreplatform.spiceadminservice.labTest.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTest;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTestResult;

public interface LabTestService {

    /**
     * This method is used to add a new Labtest.
     *
     * @param labTest
     * @return Labtest Entity
     * @author Rajkumar
     */
    public LabTest addLabTest(LabTest labTest);

    /**
     * Used to retrieve list of all LabTests in a country.
     *
     * @param requestDTO
     * @return List of LabTest entities.
     * @author Rajkumar
     */
    public List<LabTest> getAllLabTests(RequestDTO requestDTO);

    /**
     * This method is used to search labtests using labtest name.
     *
     * @param requestDTO
     * @return list of LabTest entities.
     * @author Rajkumar
     */
    public List<Map> searchLabTests(RequestDTO requestDTO);

    /**
     * This method is used to update the status of a labtest which is soft deleted.
     *
     * @param status
     * @param requestDTO
     * @return Boolean
     * @author Rajkumar
     */
    public boolean removeLabTest(RequestDTO requestDTO, boolean status);

    /**
     * This method is used to update a labTest details like name.
     *
     * @param labTest
     * @return labTest Entity
     * @author Rajkumar
     */
    public LabTest updateLabTest(LabTest labTest);

    /**
     * This method retrieves a single labtest's details.
     *
     * @param requestDTO Request object containing labtest id.
     * @return Medication Entity
     */
    public LabTest getLabTestById(RequestDTO requestDTO);

    /**
     * Used to validate labtest details.
     *
     * @param labTest
     * @return boolean
     * @author Rajkumar
     */
    public Boolean validateLabTest(LabTest labTest);

    /**
     * This method retrieves a single labtest results details.
     *
     * @param labTestId
     * @return
     * @author Rajkumar
     */
    public List<LabTestResult> getLabTestResultsById(long labTestId);

    /**
     * Returns LabTest based on name and country id and returns a single labtest.
     *
     * @param searchTerm
     * @param countryId
     * @return LabTest entity
     * @author Rajkumar
     */
    public LabTest getLabTestbyName(String searchTerm, long countryId);

    /**
     * Returns List of labtest found using list of labtest IDs.
     *
     * @param uniqueLabTestIds
     * @return List of LabTest entities
     * @author Rajkumar
     */
    public List<LabTest> getLabTestsById(Set<Long> uniqueLabTestIds);

}
