package com.mdtlabs.coreplatform.spiceservice.prescription.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotAcceptableException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.exception.SpiceValidation;
import com.mdtlabs.coreplatform.common.logger.SpiceLogger;
import com.mdtlabs.coreplatform.common.model.dto.spice.FillPrescriptionRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.FillPrescriptionResponseDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.OtherMedicationDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PrescriptionDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.PrescriptionHistoryResponse;
import com.mdtlabs.coreplatform.common.model.dto.spice.PrescriptionRequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.RequestDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.FillPrescription;
import com.mdtlabs.coreplatform.common.model.entity.spice.FillPrescriptionHistory;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientVisit;
import com.mdtlabs.coreplatform.common.model.entity.spice.Prescription;
import com.mdtlabs.coreplatform.common.model.entity.spice.PrescriptionHistory;
import com.mdtlabs.coreplatform.common.util.CommonUtil;
import com.mdtlabs.coreplatform.common.util.DateUtil;
import com.mdtlabs.coreplatform.spiceservice.ApiInterface;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;
import com.mdtlabs.coreplatform.spiceservice.patientvisit.repository.PatientVisitRepository;
import com.mdtlabs.coreplatform.spiceservice.patientvisit.service.PatientVisitService;
import com.mdtlabs.coreplatform.spiceservice.prescription.repository.FillPrescriptionHistoryRepository;
import com.mdtlabs.coreplatform.spiceservice.prescription.repository.FillPrescriptionRepository;
import com.mdtlabs.coreplatform.spiceservice.prescription.repository.PrescriptionHistoryRepository;
import com.mdtlabs.coreplatform.spiceservice.prescription.repository.PrescriptionRepository;
import com.mdtlabs.coreplatform.spiceservice.prescription.service.PrescriptionService;

import javax.validation.*;

/**
 * This service class maintains the CRUD operations for Prescription
 * 
 * @author Jeyaharini T A
 *
 */

@Service
@Validated
public class PrescriptionServiceImpl implements PrescriptionService {

	@Autowired
	private PrescriptionRepository prescriptionRepository;

	@Autowired
	private PrescriptionHistoryRepository prescriptionHistoryRepository;

	@Autowired
	private FillPrescriptionRepository fillPrescriptionRepository;

	@Autowired
	private FillPrescriptionHistoryRepository fillPrescriptionHistoryRepository;

	@Autowired
	private PatientVisitRepository patientVisitRepository;

	@Autowired
	private PatientVisitService patientVisitService;

	@Autowired
	private ApiInterface apiInterface;

	@Autowired
	private AmazonS3 s3Client;

	@Autowired
	private PatientTrackerService patientTrackerService;

	ModelMapper modelMapper = new ModelMapper();

	@Value("${application.bucket.name}")
	private String bucketName;

	/**
	 * {@inheritDoc}
	 */
	public List<Prescription> createOrUpdatePrescription(@Valid PrescriptionRequestDTO prescriptionRequestDTO) {

		if (Objects.isNull(prescriptionRequestDTO)) {
			throw new BadRequestException(1000);
		}

		if (Objects.isNull(prescriptionRequestDTO.getSignatureFile())) {
			throw new DataNotAcceptableException(29016);
		}

		if (Objects.isNull(prescriptionRequestDTO.getPatientTrackId())
				|| Objects.isNull(prescriptionRequestDTO.getPatientVisitId())) {
			throw new BadRequestException(29007);
		}

		String signature = uploadSignature(prescriptionRequestDTO.getSignatureFile(),
				prescriptionRequestDTO.getPatientTrackId(), prescriptionRequestDTO.getPatientVisitId());

		List<PrescriptionDTO> prescriptionList = prescriptionRequestDTO.getPrescriptionList();

		boolean isOtherPrescriptionPresent = prescriptionList.stream()
				.anyMatch(prescriptionDTO -> prescriptionDTO.getMedicationName().equalsIgnoreCase(Constants.OTHER));

		OtherMedicationDTO otherMedication = isOtherPrescriptionPresent ? getOtherMedication() : null;

		List<Prescription> prescriptionListToCreate = new ArrayList<>();
		List<Prescription> prescriptionListToUpdate = new ArrayList<>();
		Date date = DateUtil.getCurrentDay();
		for (PrescriptionDTO prescriptionDTO : prescriptionList) {
			if (!Objects.isNull(prescriptionDTO.getIsDeleted()) && prescriptionDTO.getIsDeleted()) {
				throw new DataNotAcceptableException(29008);
			}

			if (prescriptionDTO.getPrescribedDays() < 0) {
				throw new DataNotAcceptableException(29009);
			}

			Prescription prescription = new Prescription();
			modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull())
					.setFullTypeMatchingRequired(true);
			modelMapper.map(prescriptionRequestDTO, prescription);
			modelMapper.map(prescriptionDTO, prescription);
			prescription.setSignature(signature);
			if (prescriptionDTO.getMedicationName().equalsIgnoreCase(Constants.OTHER)) {
				prescription.setMedicationId(otherMedication.getId());
				prescription.setBrandName(otherMedication.getBrandName());
				prescription.setClassificationName(otherMedication.getClassificationName());
			}
			prescription.setEndDate(
					DateUtil.addDate(DateUtil.subtractDates(date, 1), prescriptionDTO.getPrescribedDays()));
			if (Objects.isNull(prescriptionDTO.getId())) {
				prescription.setId(null);
				prescriptionListToCreate.add(prescription);
			} else if (!Objects.isNull(prescriptionDTO.getId()) && ((Objects.isNull(prescriptionDTO.getIsDeleted()))
					|| (!Objects.isNull(prescriptionDTO.getIsDeleted()) && !prescriptionDTO.getIsDeleted()))) {
				prescriptionListToUpdate.add(prescription);
			}
		}

		List<Prescription> prescriptions = new ArrayList<>();

		if (!prescriptionListToCreate.isEmpty()) {
			prescriptions.addAll(addNewPrescriptions(prescriptionListToCreate));
		}

		if (!prescriptionListToUpdate.isEmpty()) {
			prescriptions.addAll(updatePrescriptions(prescriptionListToUpdate));
		}

		updatePrescriptionPatientVisit(prescriptionRequestDTO);
		updateMedicationPrescribed(prescriptionRequestDTO.getPatientTrackId(), Constants.BOOLEAN_TRUE);
		return prescriptions;
	}

	/**
	 * To get other medication details
	 * 
	 * @return Other Medication
	 */
	public OtherMedicationDTO getOtherMedication() {
		// As of now, sent countryid as 1
//		Medication otherMedication = medicationRepository.getOtherMedication(1, Constants.OTHER, Constants.OTHER,
//				Constants.OTHER, Constants.OTHER);
		long countryId = 1;
//		HttpEntity<String> entity = CommonUtil.getCurrentEntity();
//		HttpHeaders headers = new HttpHeaders();
//		RestTemplate restTemplate = new RestTemplate();
//		headers.setContentType(MediaType.APPLICATION_JSON);
//		ResponseEntity<Map> userResponse = restTemplate.exchange(
//				"http://192.168.20.179/admin-service/medication/other-medication/{countryId}", HttpMethod.GET,
//				new HttpEntity<>(null, headers), Map.class, countryId);
//		OtherMedicationDTO medicationDTO = modelMapper.map(userResponse.getBody().get("entity"),
//				new TypeToken<OtherMedicationDTO>() {
//				}.getType());

		ResponseEntity<OtherMedicationDTO> obj = apiInterface.getOtherMedication(countryId);

//		HttpEntity entity = new HttpEntity<>(null, headers);
//		OtherMedicationDTO medicationDTO = restService
//				.exchange("http://192.168.20.179/admin-service/medication/other-medication/" + countryId,
//						HttpMethod.GET, entity, OtherMedicationDTO.class)
//				.getBody();
		OtherMedicationDTO medicationDTO = obj.getBody();
		return medicationDTO;
	}

	/**
	 * To add new prescriptions
	 * 
	 * @param prescriptions
	 * @return List of Prescription
	 */
	public List<Prescription> addNewPrescriptions(List<Prescription> prescriptions) {
		SpiceLogger.logInfo("Adding new prescriptions");
		List<Prescription> addedPrescriptions = prescriptionRepository.saveAll(prescriptions);
		List<FillPrescription> fillPrescriptions = createOrUpdateFillPrescription(addedPrescriptions, null, true);
		addPrescriptionHistoryData(addedPrescriptions);
		addFillPrescriptionHistoryData(fillPrescriptions);
		return addedPrescriptions;
	}

	/**
	 * To update the list of Prescriptions
	 * 
	 * @param prescriptions
	 * @return
	 */
	public List<Prescription> updatePrescriptions(List<Prescription> prescriptions) {
		Set<Long> prescriptionIds = prescriptions.stream().map(prescription -> prescription.getId())
				.collect(Collectors.toSet());

		if (prescriptionIds.size() != prescriptions.size()) {
			throw new DataNotAcceptableException(29017);
		}

		List<Prescription> existingPrescriptions = prescriptionRepository.findByIds(prescriptionIds);

		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull()).setFullTypeMatchingRequired(true);

		List<Prescription> existingPrescriptionsToUpdate = new ArrayList<>();

		if (prescriptions.size() != existingPrescriptions.size()) {
			throw new SpiceValidation(29010);
		}

		for (Prescription existingPrescripiton : existingPrescriptions) {
			Prescription prescriptionToUpdate = prescriptions.stream()
					.filter(prescription -> (prescription.getId() == existingPrescripiton.getId())).findFirst()
					.orElseThrow(() -> new DataNotAcceptableException(29010));

			existingPrescriptionsToUpdate.add(prescriptionToUpdate);
		}

		List<Prescription> updatedPrescriptions = prescriptionRepository.saveAll(existingPrescriptionsToUpdate);
		List<FillPrescription> fillPrescriptions = createOrUpdateFillPrescription(updatedPrescriptions, prescriptionIds,
				false);
		addPrescriptionHistoryData(updatedPrescriptions);
		addFillPrescriptionHistoryData(fillPrescriptions);
		return updatedPrescriptions;
	}

	/**
	 * To add the prescription history request
	 * 
	 * @param prescriptions
	 * @return List of PrescriptionHistory
	 */
	public List<PrescriptionHistory> addPrescriptionHistoryData(List<Prescription> prescriptions) {
		SpiceLogger.logInfo("constructing prescription history data");
		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull()).setFullTypeMatchingRequired(true);
		List<PrescriptionHistory> prescriptionHistories = prescriptions.stream()
				.map(prescription -> modelMapper.map(prescription, PrescriptionHistory.class))
				.collect(Collectors.toList());
		prescriptionHistories.stream().forEach(prescriptionHistory -> {
			SpiceLogger.logInfo("in prescriptionHistory: " + prescriptionHistory.getId());
			// 1. While doing model mapping, as the id fields are matched, id of
			// prescription entity will be added for id of prescription history entity,
			// setting null to the id field
			prescriptionHistory.setPrescriptionId(prescriptionHistory.getId());
			prescriptionHistory.setId(null);
		});
		return prescriptionHistoryRepository.saveAll(prescriptionHistories);
	}

	/**
	 * To add or update a fill prescription
	 * 
	 * @param prescriptions
	 * @param existingPrescriptionIds
	 * @param doCreate
	 * @return List of FillPrescription
	 */
	public List<FillPrescription> createOrUpdateFillPrescription(List<Prescription> prescriptions,
			Set<Long> existingPrescriptionIds, boolean doCreate) {
		SpiceLogger.logInfo(doCreate ? "Creating fill prescriptions" : "Updating fill prescriptions");
		List<FillPrescription> existingFillPrescriptions = null;
		if (!Objects.isNull(existingPrescriptionIds)) {
			existingFillPrescriptions = fillPrescriptionRepository.findByPrescriptionIds(existingPrescriptionIds);
		}
		List<FillPrescription> fillPrescriptions = constructFillPrescriptionData(prescriptions,
				existingFillPrescriptions, doCreate);
		return fillPrescriptionRepository.saveAll(fillPrescriptions);
	}

	/**
	 * To construct the fill prescription request
	 * 
	 * @param prescriptions
	 * @return List of FillPrescription
	 */
	public List<FillPrescription> constructFillPrescriptionData(List<Prescription> prescriptions,
			List<FillPrescription> existingFillPrescriptions, boolean doCreate) {
		SpiceLogger.logInfo("constructing fill prescription data");
		List<FillPrescription> fillPrescriptions = new ArrayList<FillPrescription>();
		FillPrescription fillPrescription = null;
		for (Prescription prescription : prescriptions) {
			fillPrescription = new FillPrescription();
			if (doCreate) {
				fillPrescription.setRemainingPrescriptionDays(Constants.ZERO);
				if (!Objects.isNull(prescription.getPrescribedDays())) {
					fillPrescription.setRemainingPrescriptionDays(prescription.getPrescribedDays());
				}
			} else {
				FillPrescription existingFillPrescription = existingFillPrescriptions.stream()
						.filter(existingFillPrescriptionEntity -> (existingFillPrescriptionEntity
								.getPrescriptionId() == prescription.getId()))
						.findFirst().orElseThrow(() -> new DataNotAcceptableException(29010));
				fillPrescription.setRemainingPrescriptionDays(prescription.getPrescribedDays());
				fillPrescription.setId(existingFillPrescription.getId());
			}
			fillPrescription.setTenantId(prescription.getTenantId());
			fillPrescription.setPatientTrackId(prescription.getPatientTrackId());
			fillPrescription.setPatientVisitId(prescription.getPatientVisitId());
			fillPrescription.setPrescribedDays(prescription.getPrescribedDays());
			fillPrescription.setPrescriptionFilledDays(Constants.ZERO);
			fillPrescription.setPrescriptionId(prescription.getId());
			fillPrescriptions.add(fillPrescription);
		}
		return fillPrescriptions;
	}

	/**
	 * To update the screening status in the patient visit entity
	 * 
	 * @param prescriptionRequestDTO
	 */
	public void updatePrescriptionPatientVisit(PrescriptionRequestDTO prescriptionRequestDTO) {
		PatientVisit patientVisit = patientVisitService.getPatientVisit(prescriptionRequestDTO.getPatientVisitId(), prescriptionRequestDTO.getTenantId());
		patientVisit.setPrescription(true);
		patientVisitService.updatePatientVisit(patientVisit);
		// patientVisitRepository.updatePatientVisit(true, prescriptionRequestDTO.getPatientVisitId(),
		// 		prescriptionRequestDTO.getTenantId());
	}

	/**
	 * To add the fill prescription history
	 * 
	 * @param fillPrescriptions
	 * @return list of added FillPrescriptionHistory
	 */
	public List<FillPrescriptionHistory> addFillPrescriptionHistoryData(List<FillPrescription> fillPrescriptions) {
		SpiceLogger.logInfo("constructing fill prescription history data");
		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull()).setFullTypeMatchingRequired(true);
		List<FillPrescriptionHistory> fillPrescriptionHistories = fillPrescriptions.stream()
				.map(fillPrescription -> modelMapper.map(fillPrescription, FillPrescriptionHistory.class))
				.collect(Collectors.toList());
		fillPrescriptionHistories.stream().forEach(fillPrescriptionHistory -> {
			// 1. While doing model mapping, as the id fields are matched, id of
			// prescription entity will be added for id of prescription history entity,
			// setting null to the id field
			fillPrescriptionHistory.setFillPrescriptionId(fillPrescriptionHistory.getId());
			fillPrescriptionHistory.setId(null);
		});
		return fillPrescriptionHistoryRepository.saveAll(fillPrescriptionHistories);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PrescriptionDTO> getPrescriptions(RequestDTO prescriptionListRequestDTO) {

		if (Objects.isNull(prescriptionListRequestDTO.getPatientTrackId())) {
			throw new DataNotAcceptableException(10010);
		}
		List<Prescription> prescriptions = prescriptionRepository.findByPatientTrackIdAndIsDeleted(
				prescriptionListRequestDTO.getPatientTrackId(), prescriptionListRequestDTO.getIsDeleted());
		if (prescriptions.isEmpty()) {
			throw new DataNotFoundException(29012);
		}
		List<PrescriptionDTO> prescriptionsList = new ArrayList<>();
		for (Prescription prescription : prescriptions) {
			Date prescribedSince = DateUtil.subtractDates(prescription.getEndDate(),
					(prescription.getPrescribedDays() - 1));
			int prescriptionRemainingDays = (prescription.getPrescribedDays()
					- DateUtil.getCalendarDiff(new Date(), prescribedSince, Calendar.DATE));

			PrescriptionDTO prescriptionDTO = new PrescriptionDTO();
			modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull())
					.setFullTypeMatchingRequired(true);
			prescriptionDTO.setPrescribedSince(prescribedSince);
			prescriptionDTO.setPrescriptionRemainingDays(prescriptionRemainingDays);
			modelMapper.map(prescription, prescriptionDTO);
			prescriptionsList.add(prescriptionDTO);
		}

		return prescriptionsList;
	}

	/**
	 * {@inheritDoc}
	 */
	public PrescriptionHistoryResponse listPrescriptionHistoryData(RequestDTO prescriptionListRequestDTO) {

		PrescriptionHistoryResponse prescriptionHistoryResponse = new PrescriptionHistoryResponse();

		if (Objects.isNull(prescriptionListRequestDTO.getPatientTrackId())) {
			throw new DataNotAcceptableException(10010);
		}

		List<Date> patientVisitDates = new ArrayList<>();

		if (prescriptionListRequestDTO.isLatestRequired()
				&& !Objects.isNull(prescriptionListRequestDTO.getPatientVisitId())) {
			List<PatientVisit> patientVisits = 
					patientVisitService.getPatientVisitDates(prescriptionListRequestDTO.getPatientTrackId(), null, null, Constants.BOOLEAN_TRUE);
			patientVisitDates = patientVisits.stream().map(patientVisit -> patientVisit.getVisitDate())
					.collect(Collectors.toList());
			prescriptionListRequestDTO.setPatientVisitId(patientVisits.get(patientVisits.size() - 1).getId());
		}

		List<PrescriptionHistory> prescriptionHistories = prescriptionHistoryRepository.getPrescriptionHistory(
				prescriptionListRequestDTO.getPrescriptionId(), prescriptionListRequestDTO.getPatientVisitId(),
				prescriptionListRequestDTO.getPatientTrackId());
		prescriptionHistoryResponse.setPatientPrescription(prescriptionHistories);
		prescriptionHistoryResponse.setPrescriptionHistoryDates(patientVisitDates);
		return prescriptionHistoryResponse;
	}

	/**
	 * {@inheritDoc}
	 */

	public boolean removePrescription(RequestDTO prescriptionListRequestDTO) {
//		if (Objects.isNull(prescriptionListRequestDTO.getPatientVisitId())) {
//			throw new SpiceValidation(29013);
//		}
		Prescription prescriptionToBeDeleted = prescriptionRepository.findById(prescriptionListRequestDTO.getId())
				.orElseThrow(() -> new DataNotAcceptableException(29010));

		prescriptionToBeDeleted.setIsDeleted(Constants.BOOLEAN_TRUE);
		prescriptionToBeDeleted.setDiscontinuedOn(new Date());
		prescriptionToBeDeleted.setDiscontinuedReason(prescriptionListRequestDTO.getDiscontinuedReason());
		prescriptionToBeDeleted.setTenantId(prescriptionListRequestDTO.getTenantId());
		prescriptionRepository.save(prescriptionToBeDeleted);
		// patientTrackId should be included in the request
		updateMedicationPrescribed(prescriptionListRequestDTO.getPatientTrackId(), Constants.BOOLEAN_FALSE);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeFillPrescription(RequestDTO prescriptionListRequestDTO) {
		fillPrescriptionRepository.removeFillPrescriptions(true, prescriptionListRequestDTO.getId(),
				prescriptionListRequestDTO.getTenantId());
	}

	/**
	 * {@inheritDoc}
	 */
	public List<FillPrescriptionResponseDTO> getFillPrescriptions(SearchRequestDTO searchRequestDTO) {
		if (Objects.isNull(searchRequestDTO.getPatientTrackId())) {
			throw new DataNotAcceptableException(10010);
		}
		List<FillPrescription> fillPrescriptions = fillPrescriptionRepository
				.findByPatientTrackIdAndRemainingPrescriptionDaysGreaterThan(searchRequestDTO.getPatientTrackId(),
						Constants.ZERO);

		Set<Long> prescriptionIds = fillPrescriptions.stream()
				.map(fillPrescription -> fillPrescription.getPrescriptionId()).collect(Collectors.toSet());
		List<Prescription> prescriptions = prescriptionRepository.findByIds(prescriptionIds);

		List<FillPrescriptionResponseDTO> fillPrescriptionResponseDTOs = new ArrayList<>();

		for (FillPrescription fillPrescription : fillPrescriptions) {
			Prescription prescription = prescriptions.stream()
					.filter(prescriptionEntity -> (prescriptionEntity.getId() == fillPrescription.getPrescriptionId()))
					.findFirst().orElseThrow(() -> new DataNotAcceptableException(29010));

			FillPrescriptionResponseDTO fillPrescriptionResponseDTO = new FillPrescriptionResponseDTO();
			fillPrescriptionResponseDTO.setId(fillPrescription.getId());
			fillPrescriptionResponseDTO.setDosageFormName(prescription.getDosageFormName());
			fillPrescriptionResponseDTO.setDosageFrequencyName(prescription.getDosageFrequencyName());
			fillPrescriptionResponseDTO.setDosageUnitName(prescription.getDosageUnitName());
			fillPrescriptionResponseDTO.setDosageUnitValue(prescription.getDosageUnitValue());
			fillPrescriptionResponseDTO.setMedicationName(prescription.getMedicationName());
			fillPrescriptionResponseDTO.setClassificationName(prescription.getClassificationName());
			fillPrescriptionResponseDTO.setInstructionNote(prescription.getInstructionNote());
			fillPrescriptionResponseDTO.setEndDate(prescription.getEndDate());
			fillPrescriptionResponseDTO.setCreatedAt(prescription.getCreatedAt());
			fillPrescriptionResponseDTO.setBrandName(prescription.getBrandName());
			fillPrescriptionResponseDTO.setPrescription(prescription.getId());
			fillPrescriptionResponseDTO.setTenantId(fillPrescription.getTenantId());
			fillPrescriptionResponseDTO.setRemainingPrescriptionDays(fillPrescription.getRemainingPrescriptionDays());
			fillPrescriptionResponseDTOs.add(fillPrescriptionResponseDTO);
		}

		return fillPrescriptionResponseDTOs;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<FillPrescription> updateFillPrescription(FillPrescriptionRequestDTO fillPrescriptionRequestDTO) {
		if (Objects.isNull(fillPrescriptionRequestDTO.getPatientTrackId())
				|| Objects.isNull(fillPrescriptionRequestDTO.getPatientVisitId())) {
			throw new DataNotAcceptableException(29007);
		}

		List<FillPrescription> fillPrescriptions = fillPrescriptionRequestDTO.getFillPrescriptions();

		List<Long> fillPrescriptionIds = fillPrescriptions.stream().map(fillPrescription -> fillPrescription.getId())
				.collect(Collectors.toList());

		List<FillPrescription> existingFillPrescriptions = fillPrescriptionRepository
				.findByIdsAndIsDeleted(fillPrescriptionIds, Constants.BOOLEAN_FALSE);

		List<FillPrescription> fillPrescriptionsToUpdate = new ArrayList<>();

		for (FillPrescription fillPrescription : fillPrescriptions) {
			FillPrescription existingFillPrescription = existingFillPrescriptions.stream()
					.filter(existing -> existing.getId() == fillPrescription.getId()).findFirst()
					.orElseThrow(() -> new DataNotFoundException(29014));

			if (existingFillPrescription.getRemainingPrescriptionDays() < fillPrescription
					.getPrescriptionFilledDays()) {
				throw new DataNotAcceptableException(29015);
			}
			int remainingPrescriptionDays = existingFillPrescription.getRemainingPrescriptionDays()
					- fillPrescription.getPrescriptionFilledDays();

			existingFillPrescription.setPrescriptionId(fillPrescription.getPrescriptionId());
			existingFillPrescription.setPrescriptionFilledDays(fillPrescription.getPrescriptionFilledDays());
			existingFillPrescription.setRemainingPrescriptionDays(remainingPrescriptionDays);
			existingFillPrescription.setPatientVisitId(fillPrescriptionRequestDTO.getPatientVisitId());
			existingFillPrescription.setTenantId(fillPrescriptionRequestDTO.getTenantId());
			existingFillPrescription.setPatientTrackId(fillPrescriptionRequestDTO.getPatientTrackId());
			fillPrescriptionsToUpdate.add(existingFillPrescription);
		}

		fillPrescriptions = fillPrescriptionRepository.saveAll(fillPrescriptionsToUpdate);
		addFillPrescriptionHistory(fillPrescriptions);
		updatePrescriptionDetails(fillPrescriptionRequestDTO);

//		List<FillPrescription> existingFillPrescriptions = fillPrescriptionRepository
//				.findByIdsAndIsDeleted(fillPrescriptionIds, Constants.BOOLEAN_FALSE);

//		if (fillPrescriptionRepository.findByPatientTrackIdAndRemainingPrescriptionDaysGreaterThan(
//				fillPrescriptionRequestDTO.getPatientTrackId(), Constants.ZERO).isEmpty()) {
//			patientTrackerService.updateForFillPrescription(fillPrescriptionRequestDTO.getPatientTrackId(),
//					Constants.BOOLEAN_FALSE);
//		}

		updateMedicationPrescribed(fillPrescriptionRequestDTO.getPatientTrackId(), Constants.BOOLEAN_FALSE);

		return fillPrescriptions;
	}

	public void updateMedicationPrescribed(Long patientTrackId, boolean isMedicationPrescribed) {
		if (!isMedicationPrescribed) {
			if (fillPrescriptionRepository
					.findByPatientTrackIdAndRemainingPrescriptionDaysGreaterThan(patientTrackId, Constants.ZERO)
					.isEmpty()) {
				patientTrackerService.updateForFillPrescription(patientTrackId, Constants.BOOLEAN_FALSE);
			}
		} else {
			patientTrackerService.updateForFillPrescription(patientTrackId, Constants.BOOLEAN_TRUE);
		}
	}

	/**
	 * To add fill prescription history
	 * 
	 * @param fillPrescriptions
	 * @return list of added fill prescription history
	 */
	public List<FillPrescriptionHistory> addFillPrescriptionHistory(List<FillPrescription> fillPrescriptions) {

		List<FillPrescriptionHistory> fillPrescriptionHistories = new ArrayList<>();

		for (FillPrescription fillPrescription : fillPrescriptions) {
			FillPrescriptionHistory fillPrescriptionHistory = new FillPrescriptionHistory();
			fillPrescriptionHistory.setPrescriptionId(fillPrescription.getPrescriptionId());
			fillPrescriptionHistory.setFillPrescriptionId(fillPrescription.getId());
			fillPrescriptionHistory
					.setRemainingPrescriptionDays(!Objects.isNull(fillPrescription.getRemainingPrescriptionDays())
							? fillPrescription.getRemainingPrescriptionDays()
							: 0);
			fillPrescriptionHistory.setTenantId(fillPrescription.getTenantId());
			fillPrescriptionHistory
					.setPrescriptionFilledDays(!Objects.isNull(fillPrescription.getPrescriptionFilledDays())
							? fillPrescription.getPrescriptionFilledDays()
							: 0);
			fillPrescriptionHistory.setPatientVisitId(fillPrescription.getPatientVisitId());
			fillPrescriptionHistory.setPrescribedDays(fillPrescription.getPrescribedDays());
			fillPrescriptionHistory.setPatientTrackId(fillPrescription.getPatientTrackId());
			fillPrescriptionHistories.add(fillPrescriptionHistory);
		}

		return fillPrescriptionHistoryRepository.saveAll(fillPrescriptionHistories);

	}

	/**
	 * To update prescription details
	 * 
	 * @param fillPrescriptionRequestDTO
	 * @return list of updated prescriptions
	 */
	public List<Prescription> updatePrescriptionDetails(FillPrescriptionRequestDTO fillPrescriptionRequestDTO) {

		List<FillPrescription> fillPrescriptions = fillPrescriptionRequestDTO.getFillPrescriptions();

		List<FillPrescription> instuctionUpdatedFillPrescriptions = fillPrescriptions.stream()
				.filter(fillPrescription -> (fillPrescription.getIsInstructionUpdated() == Constants.BOOLEAN_TRUE))
				.collect(Collectors.toList());

		List<Prescription> prescriptions = new ArrayList<>();
		if (instuctionUpdatedFillPrescriptions.isEmpty()) {
			return prescriptions;
		}

		Set<Long> prescriptionIds = instuctionUpdatedFillPrescriptions.stream()
				.map(fillPrescription -> fillPrescription.getPrescriptionId()).collect(Collectors.toSet());
		List<Prescription> existingPrescriptions = prescriptionRepository.findByIds(prescriptionIds);

		List<Prescription> prescriptionsToUpdate = new ArrayList<>();

		for (Prescription existingPrescription : existingPrescriptions) {
			FillPrescription fillPrescription = instuctionUpdatedFillPrescriptions.stream()
					.filter(fillPrescriptionEntity -> fillPrescriptionEntity.getPrescriptionId() == existingPrescription
							.getId())
					.findFirst().orElseThrow();
			existingPrescription.setInstructionNote(fillPrescription.getInstructionNote());
			prescriptionsToUpdate.add(existingPrescription);
		}
		List<Prescription> updatedPrescriptions = prescriptionRepository.saveAll(prescriptionsToUpdate);
		addPrescriptionHistoryData(updatedPrescriptions);
		return updatedPrescriptions;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<FillPrescriptionHistory> getRefillPrescriptionHistory(SearchRequestDTO searchRequestDTO) {
		List<FillPrescriptionHistory> fillPrescriptionHistories = new ArrayList<>();

		if (Objects.isNull(searchRequestDTO.getPatientTrackId())
				|| Objects.isNull(searchRequestDTO.getLastRefillVisitId())) {
			throw new DataNotAcceptableException(29007);
		}
		fillPrescriptionHistories = fillPrescriptionHistoryRepository.getFillPrescriptionHistory(
				searchRequestDTO.getPatientTrackId(), searchRequestDTO.getLastRefillVisitId(), Constants.ZERO);

		return fillPrescriptionHistories;
	}

	/**
	 * {@inheritDoc}
	 */
	public String uploadSignature(MultipartFile file, Long patientTrackId, Long patientVisitId) {
		File fileObj = convertMultipartFileToFile(file);
		Instant instant = Instant.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.SIGNATURE_DATE_FORMAT)
				.withZone(ZoneId.of("UTC"));
		String timestamp = formatter.format(instant);
		String fileName = patientTrackId + "_" + patientVisitId + "_" + timestamp + "_prescsign.jpeg";
		s3Client.putObject(new PutObjectRequest(bucketName, fileName, fileObj));
		String url = s3Client.getUrl(bucketName, fileName).toString();
		fileObj.delete();
		return url;
	}

	private File convertMultipartFileToFile(MultipartFile file) {
		File convertedFile = new File(file.getOriginalFilename());
		try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
			fos.write(file.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return convertedFile;
	}

}
