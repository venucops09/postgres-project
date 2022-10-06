package com.mdtlabs.coreplatform.spiceadminservice.labTest.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.Condition;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataConflictException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.exception.SpiceValidation;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTest;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTestResult;
import com.mdtlabs.coreplatform.common.util.Pagination;
import com.mdtlabs.coreplatform.spiceadminservice.labTest.repository.LabTestRepository;
import com.mdtlabs.coreplatform.spiceadminservice.labTest.service.LabTestService;


@Service
public class LabTestServiceImpl implements LabTestService {

    @Autowired
    private LabTestRepository labTestRepository;


    private ModelMapper mapper = new ModelMapper();
    
    /**
     * {@inheritDoc}
     */
    public LabTest addLabTest(LabTest labTest) {
        if (Objects.isNull(labTest)) {
            throw new BadRequestException(12006);
        } else {
            if (null != labTest.getLabTestResults() && !labTest.getLabTestResults().isEmpty()) {
                labTest.setResultTemplate(true);
            }

            LabTest existingLabtest = labTestRepository.findByCountryIdAndNameAndIsDeleted(labTest.getCountryId(),
                    labTest.getName(), false);

            if (!Objects.isNull(existingLabtest)) {
                throw new DataConflictException(18000);
            }
            if (labTest.isResultTemplate()) {
                Set<String> resultNames = new HashSet<String>();

                for (LabTestResult result : labTest.getLabTestResults()) {
                    result.setTenantId(labTest.getTenantId());
                    resultNames.add(result.getName());
                }
                if (labTest.getLabTestResults().size() != resultNames.size()) {
                    throw new DataConflictException(18007);
                }
            }
            LabTest labtestResponse = labTestRepository.save(labTest);
            return labtestResponse;
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<LabTest> getAllLabTests(RequestDTO requestObject) {
    
		Pageable pageable = Pagination.setPagination(requestObject.getSkip(), requestObject.getLimit(), Sort.by("updatedAt").descending());
        Page<LabTest> labTests;

        String formattedSearchTerm = requestObject.getSearchTerm();
        if (!Objects.isNull(requestObject.getSearchTerm()) && 0 < requestObject.getSearchTerm().length()) {
            formattedSearchTerm = requestObject.getSearchTerm().replaceAll("[^a-zA-Z0-9]*", "");
        }
        labTests = labTestRepository.getAllLabTests(formattedSearchTerm, requestObject.getCountryId(),
                requestObject.getTenantId(), pageable);
        return labTests.stream().collect(Collectors.toList());

    }

    /**
     * {@inheritDoc}
     */
    public List<Map> searchLabTests(RequestDTO requestDTO) {
        String searchTerm = requestDTO.getSearchTerm();
        if (Objects.isNull(searchTerm) || 0 == searchTerm.length()) {
            throw new DataNotAcceptableException(18008);
        }
        String formattedSearchTerm = searchTerm.replaceAll("[^a-zA-Z0-9 ]*", "");
        List<LabTest> labTests = labTestRepository.searchLabTests(formattedSearchTerm, requestDTO.getCountryId(), requestDTO.getIsActive());
        return labTests.stream().map(labTest -> Map.of("id", labTest.getId(), "name", labTest.getName(), "country", labTest.getCountryId())).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    public boolean removeLabTest(RequestDTO requestDTO, boolean status) {
        LabTest labTest = labTestRepository.findByIdAndIsDeleted(requestDTO.getId(), Constants.BOOLEAN_FALSE);
        if (Objects.isNull(labTest)) {
            throw new DataNotFoundException(18001);
        } else {
            labTest.setDeleted(Constants.BOOLEAN_TRUE);
            if (!labTest.getLabTestResults().isEmpty()) {
                for (LabTestResult labTestResult : labTest.getLabTestResults()) {
                    labTestResult.setDeleted(status);
                }
            }
            return !Objects.isNull(labTestRepository.save(labTest));
        }
    }

    /**
     * {@inheritDoc}
     */
    // public LabTest updateLabTest(LabTest labTest) {
    //     <<<<<<< Updated upstream
//         if (Objects.isNull(labTest)) {
//             throw new BadRequestException(12006);
//         } else {
//             validateLabTest(labTest);
//             LabTest existingLabTest = labTestRepository.findByIdAndIsDeleted(labTest.getId(), false);
//             if (Objects.isNull(existingLabTest)) {
//                 throw new DataNotFoundException(18001);
//             }
//             if (!(existingLabTest.getName()).equals(labTest.getName())) {
//                 throw new DataNotAcceptableException(18000);
//             }
//             if ((existingLabTest.getCountryId()) != (labTest.getCountryId())) {
//                 throw new DataNotAcceptableException(12010);
// =======
    //         validateLabTestResults(labTest);
    //         List<LabTestResult> oldInactiveResults = existingLabTest.getLabTestResults().stream()
    //                 .filter(labTestResult -> !labTestResult.getIsActive()).collect(Collectors.toList());
    //         labTest.getLabTestResults().addAll(oldInactiveResults);
    //         return labTestRepository.save(labTest);
    //     }
    // }

    public LabTest updateLabTest(LabTest labTest) {
        LabTest existingLabTest = validateLabTestRequest(labTest);
        if(!Objects.isNull(labTest.getLabTestResults()) && !labTest.getLabTestResults().isEmpty()) {
            validateLabTestResultsAndUpdate(labTest, existingLabTest);
        }
        // labTest.setLabTestResults(null);
        mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        mapper.createTypeMap(LabTest.class, LabTest.class).addMappings(map -> map.skip(LabTest::setLabTestResults));
        mapper.map(labTest, existingLabTest);
        return labTestRepository.save(existingLabTest);
    }

    private void validateLabTestResultsAndUpdate(LabTest labTest, LabTest existingLabTest) {
        mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        List<LabTestResult> newLabTestResults = new ArrayList<>();
        List<Long> deletedLabTestResults = new ArrayList<>();
        Map<Long, LabTestResult> updatedLabTestResults = new HashMap<>();

        Map<Long, String> oldResults = existingLabTest.getLabTestResults().stream().filter(
            result -> !result.isDeleted()
        ).collect(Collectors.toMap(LabTestResult::getId, LabTestResult::getName));    

        List<LabTestResult> existingNewLabTestResult = new ArrayList<>();
        List<String> existingUpdatedLabTestResult = new ArrayList<>();

        for (LabTestResult result : labTest.getLabTestResults()) {
            if (Objects.isNull(result.getId())) {
                if (oldResults.values().contains(result.getName())) {
                    existingNewLabTestResult.add(result);
                } else {
                    newLabTestResults.add(result);
                    // existingLabTest.getLabTestResults().add(result);
                }
                // newLabTestResults.add(result);
            } else if (oldResults.keySet().contains(result.getId()) && result.isDeleted()) {
                deletedLabTestResults.add(result.getId());
                // existingLabTest.getLabTestResults().stream().findAny()

            } else if (oldResults.keySet().contains(result.getId()) && !result.isDeleted()) {
                if (oldResults.values().contains(result.getName()) && oldResults.get(result.getId()) != result.getName()) {
                    existingUpdatedLabTestResult.add(result.getName());
                } else {
                    updatedLabTestResults.put(result.getId(), result);
                }
            }
        }  
        // List<LabTestResult> exist = 
        List<String> names = newLabTestResults.stream().map(
            name -> name.getName()
        ).collect(Collectors.toList());
        if (
            updatedLabTestResults.values().stream().anyMatch(
                data -> names.contains(data.getName())
            )
        ) {
            throw new SpiceValidation();
        } else if (!existingNewLabTestResult.isEmpty() || !existingNewLabTestResult.isEmpty()  ) {
            throw new SpiceValidation();
        } else {
            labTest.getLabTestResults().addAll(newLabTestResults);
            labTest.getLabTestResults().stream().forEach(result -> {
                if(deletedLabTestResults.contains(result.getId())) {
                    result.setDeleted(true);
                }
                if (updatedLabTestResults.keySet().contains(result.getId())) {
                    mapper.map(updatedLabTestResults.get(result.getId()), result);
                }
            }   
            );
        }
    }

    private LabTest validateLabTestRequest(LabTest labTest) {
        if (Objects.isNull(labTest.getId())) {
            //labtest id not exist 
            throw new SpiceValidation();
        }
        LabTest existingLabTest = labTestRepository.findByIdAndIsDeleted(labTest.getId(), Constants.BOOLEAN_FALSE);
        if (Objects.isNull(existingLabTest)) {
            //labtest doesn't exist
            throw new SpiceValidation();
        }
        if (!Objects.isNull(labTest.getName()) && !existingLabTest.getName().equals(labTest.getName())) {
            //Lab Test name should not be changed.
            throw new SpiceValidation();
        }
        if (!Objects.isNull(labTest.getCountryId()) && existingLabTest.getCountryId() != existingLabTest.getCountryId()) {
            //%s should not be changed.
            throw new SpiceValidation();
        }
        return existingLabTest;
    }

    /**
     * {@inheritDoc}
     */
    public LabTest getLabTestById(RequestDTO requestDTO) {
        LabTest labTest = labTestRepository.findByIdAndIsDeleted(requestDTO.getId(), false);
        if (Objects.isNull(labTest)) {
            throw new DataNotFoundException(18001);
        } else {
            labTest.setLabTestResults(labTest.getLabTestResults().stream()
                    .filter(labTestResult -> !labTestResult.isDeleted()).collect(Collectors.toSet()));
        }
        return labTest;
    }

    /**
     * {@inheritDoc}
     */
    public Boolean validateLabTest(LabTest labTest) {
        LabTest labTestCountryDetail = labTestRepository.findByCountryIdAndNameAndIsDeleted(labTest.getCountryId(),
                labTest.getName(), false);
        if (!Objects.isNull(labTestCountryDetail) && labTest.getId() != labTestCountryDetail.getId()) {
            throw new DataConflictException(18000);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void validateLabTestResults(LabTest labTest) {
        if (!labTest.getLabTestResults().isEmpty()) {
            for (LabTestResult labTestResult : labTest.getLabTestResults()) {
                if (0 != labTestResult.getId() && labTestResult.isDeleted()) {
                    labTestResult.setActive(false);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<LabTestResult> getLabTestResultsById(long labTestId) {
        Set<LabTestResult> labTestResults = labTestRepository.findByIdAndIsDeleted(labTestId, false)
                .getLabTestResults();
        System.out.println("--------------------------------labTestResults" + labTestResults);
        return labTestResults.stream().filter(labTestResult -> labTestResult.isActive() == true)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    public LabTest getLabTestbyName(String searchTerm, long countryId) {
        return labTestRepository.findByNameIgnoreCaseAndCountryId(searchTerm, countryId);
    }

    /**
     * {@inheritDoc}
     */
    public List<LabTest> getLabTestsById(Set<Long> uniqueLabTestIds) {
        List<LabTest> labTests = new ArrayList<>();
         for (Long id: uniqueLabTestIds) {
             LabTest labTest = labTestRepository.findByIdAndIsDeleted(id, Constants.BOOLEAN_FALSE);
             if (!Objects.isNull(labTest)) {
                 labTests.add(labTest);
             }
         }
//        labTests = labTestRepository.findAllById(uniqueLabTestIds);
        System.out.println("++++++++++++++++++++labTests in getLabTestsById" + labTests);
        return labTests;
    }

}
