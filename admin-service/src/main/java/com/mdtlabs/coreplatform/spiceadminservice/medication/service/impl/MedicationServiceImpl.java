package com.mdtlabs.coreplatform.spiceadminservice.medication.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataConflictException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.Medication;
import com.mdtlabs.coreplatform.common.util.Pagination;
import com.mdtlabs.coreplatform.spiceadminservice.medication.repository.MedicationRepository;
import com.mdtlabs.coreplatform.spiceadminservice.medication.service.MedicationService;

/**
 * This class implements the MedicationService interface and contains actual
 * business logic to perform operations on medication entities.
 *
 * @author Niraimathi
 */
@Service
public class MedicationServiceImpl implements MedicationService {

    @Autowired
    private MedicationRepository medicationRepository;

    private ModelMapper mapper = new ModelMapper();

    /**
     * {@inheritDoc}
     */
    public List<Medication> addMedication(List<Medication> medications) {
        if (medications.isEmpty()) {
            throw new BadRequestException(12006);
        }
        return medicationRepository.saveAll(medications);
    }

    /**
     * {@inheritDoc}
     */
    public Medication updateMedication(Medication medication) {
        if (Objects.isNull(medication)) {
            throw new BadRequestException(12006);
        } else {
            validateMedication(medication);
            Medication existingMedication = medicationRepository.getMedicationByIdAndIsDeleted(medication.getId(),
                    false);

            if (Objects.isNull(existingMedication)) {
                throw new DataNotFoundException(12008);
            }
            if (!(existingMedication.getMedicationName()).equals(medication.getMedicationName())) {
                throw new DataNotAcceptableException(12009);
            }
            if ((existingMedication.getCountryId()) != (medication.getCountryId())) {
                throw new DataNotAcceptableException(12010);
            }
            mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
            medication.setCreatedBy(null);
            medication.setUpdatedBy(null);
            mapper.map(medication, existingMedication);
            return medicationRepository.save(existingMedication);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Boolean validateMedication(Medication medication) {
        Medication medicationCountryDetail = medicationRepository.getMedicationByFields(
                medication.getClassificationId(), medication.getBrandId(), medication.getDosageFormId(),
                medication.getCountryId(), medication.getMedicationName());
        if (!Objects.isNull(medicationCountryDetail) && medication.getId() != medicationCountryDetail.getId()) {
            throw new DataConflictException(12009);
        }
        return Objects.isNull(medicationCountryDetail) || (medication.getId() == medicationCountryDetail.getId());
    }

    /**
     * {@inheritDoc}
     */
    public Medication getMedicationById(RequestDTO requestDTO) {
        Medication medication = medicationRepository.findByIdAndIsDeletedFalseAndTenantId(requestDTO.getId(),
                requestDTO.getTenantId());
        if (Objects.isNull(medication)) {
            throw new DataNotFoundException();
        }
        return medication;
    }

    /**
     * {@inheritDoc}
     */
    public List<Medication> getAllMedications(RequestDTO requestObject) {
        String sortField = Objects.isNull(requestObject.getSortField()) ||
                requestObject.getSortField().isBlank() ? FieldConstants.MODIFIED_AT : requestObject.getSortField();
        Direction sortDirection = 0 != requestObject.getSortOrder() &&
                -1 == requestObject.getSortOrder() ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = Pagination.setPagination(requestObject.getSkip(), requestObject.getLimit(),
                Sort.by(sortDirection, sortField));

        Page<Medication> medications;
        String formattedSearchTerm = requestObject.getSearchTerm();
        if (!Objects.isNull(requestObject.getSearchTerm()) && 0 < requestObject.getSearchTerm().length()) {
            formattedSearchTerm = requestObject.getSearchTerm().replaceAll("[^a-zA-Z0-9 ]*", "");
        }
        medications = medicationRepository.getAllMedications(formattedSearchTerm, requestObject.getCountryId(),
                requestObject.getTenantId(), pageable);
        return medications.stream().collect(Collectors.toList());

    }

    /**
     * {@inheritDoc}
     */
    public Boolean deleteMedicationById(RequestDTO requestDTO) {
        return (1 == medicationRepository.updateMedicationById(Constants.BOOLEAN_TRUE, requestDTO.getId(),
                requestDTO.getTenantId()));
    }

    /**
     * {@inheritDoc}
     */
    public List<Medication> searchMedications(RequestDTO requestObject) {
// >>>>>>> Stashed changes
//        This validation is not needed. countryid should be get from user data.

//        if (Objects.isNull(requestObject.getCountryId())) {
//            throw new SpiceValidation(1000);
//        }
		String searchTerm = requestObject.getSearchTerm();
		if (Objects.isNull(searchTerm) || 0 == searchTerm.length()) {
			throw new DataNotAcceptableException(18008);
		}
		String formattedSearchTerm = searchTerm.replaceAll("[^a-zA-Z0-9]*", "");
		return medicationRepository.searchMedications(formattedSearchTerm, requestObject.getCountryId());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Medication getOtherMedication(long countryId) {
		return medicationRepository.getOtherMedication(countryId, Constants.OTHER, Constants.OTHER, Constants.OTHER,
				Constants.OTHER);
	}
}
