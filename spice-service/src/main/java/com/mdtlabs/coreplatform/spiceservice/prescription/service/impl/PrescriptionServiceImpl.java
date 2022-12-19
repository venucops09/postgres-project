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

import javax.validation.Valid;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.contexts.UserContextHolder;
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
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientVisit;
import com.mdtlabs.coreplatform.common.model.entity.spice.Prescription;
import com.mdtlabs.coreplatform.common.model.entity.spice.PrescriptionHistory;
import com.mdtlabs.coreplatform.common.util.DateUtil;
import com.mdtlabs.coreplatform.spiceservice.ApiInterface;
import com.mdtlabs.coreplatform.spiceservice.patientTracker.service.PatientTrackerService;
import com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.service.PatientTreatmentPlanService;
import com.mdtlabs.coreplatform.spiceservice.patientvisit.service.PatientVisitService;
import com.mdtlabs.coreplatform.spiceservice.prescription.repository.PrescriptionHistoryRepository;
import com.mdtlabs.coreplatform.spiceservice.prescription.repository.PrescriptionRepository;
import com.mdtlabs.coreplatform.spiceservice.prescription.service.PrescriptionService;

@Service
@Validated
public class PrescriptionServiceImpl implements PrescriptionService {

	@Autowired
	private PrescriptionRepository prescriptionRepository;

	@Autowired
	private PrescriptionHistoryRepository prescriptionHistoryRepository;

	@Autowired
	private PatientVisitService patientVisitService;

	@Autowired
	private ApiInterface apiInterface;

	@Autowired
	private AmazonS3 s3Client;

	@Autowired
	private PatientTrackerService patientTrackerService;

	@Autowired
	private PatientTreatmentPlanService patientTreatmentPlanService;

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
			prescription
					.setEndDate(DateUtil.addDate(DateUtil.subtractDates(date, 1), prescriptionDTO.getPrescribedDays()));
			if (Objects.isNull(prescriptionDTO.getId())) {
				prescription.setId(null);
				prescription.setRemainingPrescriptionDays(
						!Objects.isNull(prescription.getPrescribedDays()) ? prescription.getPrescribedDays()
								: Constants.ZERO);
				prescription.setPrescriptionFilledDays(Constants.ZERO);
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
		updateMedicationPrescribed(prescriptionRequestDTO.getPatientTrackId(), Constants.BOOLEAN_TRUE,
				Constants.BOOLEAN_TRUE);
		return prescriptions;
	}

	/**
	 * To get other medication details
	 * 
	 * @return Other Medication
	 */
	public OtherMedicationDTO getOtherMedication() {

		long countryId = UserContextHolder.getUserDto().getCountry().getId();
		ResponseEntity<OtherMedicationDTO> obj = apiInterface
				.getOtherMedication(Constants.BEARER + UserContextHolder.getUserDto().getAuthorization(),UserContextHolder.getUserDto().getTenantId(), countryId);
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
//		List<FillPrescription> fillPrescriptions = createOrUpdateFillPrescription(addedPrescriptions, null, true);
		addPrescriptionHistoryData(addedPrescriptions, false);
//		addFillPrescriptionHistoryData(fillPrescriptions);
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

		List<Prescription> existingPrescriptions = prescriptionRepository.getPrescriptions(prescriptionIds);

		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull()).setFullTypeMatchingRequired(true);

		List<Prescription> existingPrescriptionsToUpdate = new ArrayList<>();

		if (prescriptions.size() != existingPrescriptions.size()) {
			throw new SpiceValidation(29010);
		}

		for (Prescription existingPrescripiton : existingPrescriptions) {
			Prescription prescriptionToUpdate = prescriptions.stream()
					.filter(prescription -> (prescription.getId() == existingPrescripiton.getId())).findFirst()
					.orElseThrow(() -> new DataNotAcceptableException(29010));

			if (existingPrescripiton.getPrescribedDays() != prescriptionToUpdate.getPrescribedDays()) {
				prescriptionToUpdate.setPrescriptionFilledDays(Constants.ZERO);
				prescriptionToUpdate.setRemainingPrescriptionDays(prescriptionToUpdate.getPrescribedDays());
			}

			existingPrescriptionsToUpdate.add(prescriptionToUpdate);
		}

		List<Prescription> updatedPrescriptions = prescriptionRepository.saveAll(existingPrescriptionsToUpdate);
//		List<FillPrescription> fillPrescriptions = createOrUpdateFillPrescription(updatedPrescriptions, prescriptionIds,
//				false);
		addPrescriptionHistoryData(updatedPrescriptions, false);
//		addFillPrescriptionHistoryData(fillPrescriptions);
		return updatedPrescriptions;
	}

	/**
	 * To add the prescription history request
	 * 
	 * @param prescriptions
	 * @return List of PrescriptionHistory
	 */
	public List<PrescriptionHistory> addPrescriptionHistoryData(List<Prescription> prescriptions,
			boolean addRefillDate) {
		SpiceLogger.logInfo("constructing prescription history data");
		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull()).setFullTypeMatchingRequired(true);
		List<PrescriptionHistory> prescriptionHistories = prescriptions.stream()
				.map(prescription -> modelMapper.map(prescription, PrescriptionHistory.class))
				.collect(Collectors.toList());

//		List<Long> prescriptionIds = prescriptionHistories.stream().map(presc -> presc.getPrescriptionId())
//				.collect(Collectors.toList());

		if (addRefillDate) {
			prescriptionHistories.stream().forEach(presc -> presc.setLastRefillDate(new Date()));
//			prescriptionHistoriesToUpdate.addAll(prescriptionHistories);
		}
//		else {
//			List<PrescriptionHistory> existingPrescriptions = prescriptionHistoryRepository
//					.getLastRefilledPrescriptions(prescriptionIds);
//			if (!existingPrescriptions.isEmpty()) {
//
//				for (PrescriptionHistory prescHistory : prescriptionHistories) {
//					PrescriptionHistory prescription = existingPrescriptions.stream()
//							.filter(presc -> (presc.getPrescriptionId() == prescHistory.getPrescriptionId()))
//							.findFirst().orElse(new PrescriptionHistory());
//					if (!Objects.isNull(prescription.getLastRefillDate())) {
//						prescHistory.setLastRefillDate(prescription.getLastRefillDate());
//					}
//					prescriptionHistoriesToUpdate.add(prescHistory);
//				}
//			}
//
//		}

		System.out.println("prescriptionHistories: " + prescriptionHistories.toString());
		return prescriptionHistoryRepository.saveAll(prescriptionHistories);
	}

	/**
	 * To construct the fill prescription request
	 * 
	 * @param prescriptions
	 * @return List of FillPrescription
	 */
//	public List<FillPrescription> constructFillPrescriptionData(List<Prescription> prescriptions,
//			List<FillPrescription> existingFillPrescriptions, boolean doCreate) {
//		SpiceLogger.logInfo("constructing fill prescription data");
//		List<FillPrescription> fillPrescriptions = new ArrayList<FillPrescription>();
//		FillPrescription fillPrescription = null;
//		for (Prescription prescription : prescriptions) {
//			fillPrescription = new FillPrescription();
//			if (doCreate) {
//				fillPrescription.setRemainingPrescriptionDays(Constants.ZERO);
//				if (!Objects.isNull(prescription.getPrescribedDays())) {
//					fillPrescription.setRemainingPrescriptionDays(prescription.getPrescribedDays());
//				}
//			} else {
//				FillPrescription existingFillPrescription = existingFillPrescriptions.stream()
//						.filter(existingFillPrescriptionEntity -> (existingFillPrescriptionEntity
//								.getPrescriptionId() == prescription.getId()))
//						.findFirst().orElseThrow(() -> new DataNotAcceptableException(29010));
//				fillPrescription.setRemainingPrescriptionDays(prescription.getPrescribedDays());
//				fillPrescription.setId(existingFillPrescription.getId());
//			}
//			fillPrescription.setTenantId(prescription.getTenantId());
//			fillPrescription.setPatientTrackId(prescription.getPatientTrackId());
//			fillPrescription.setPatientVisitId(prescription.getPatientVisitId());
//			fillPrescription.setPrescribedDays(prescription.getPrescribedDays());
//			fillPrescription.setPrescriptionFilledDays(Constants.ZERO);
//			fillPrescription.setPrescriptionId(prescription.getId());
//			fillPrescriptions.add(fillPrescription);
//		}
//		return fillPrescriptions;
//	}

	/**
	 * To update the screening status in the patient visit entity
	 * 
	 * @param prescriptionRequestDTO
	 */
	public void updatePrescriptionPatientVisit(PrescriptionRequestDTO prescriptionRequestDTO) {
		PatientVisit patientVisit = patientVisitService.getPatientVisit(prescriptionRequestDTO.getPatientVisitId(),
				prescriptionRequestDTO.getTenantId());
		patientVisit.setPrescription(true);
		patientVisitService.updatePatientVisit(patientVisit);
		// patientVisitRepository.updatePatientVisit(true,
		// prescriptionRequestDTO.getPatientVisitId(),
		// prescriptionRequestDTO.getTenantId());
	}

	/**
	 * To add the fill prescription history
	 * 
	 * @param fillPrescriptions
	 * @return list of added FillPrescriptionHistory
	 */
//	public List<FillPrescriptionHistory> addFillPrescriptionHistoryData(List<FillPrescription> fillPrescriptions) {
//		SpiceLogger.logInfo("constructing fill prescription history data");
//		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull()).setFullTypeMatchingRequired(true);
//		List<FillPrescriptionHistory> fillPrescriptionHistories = fillPrescriptions.stream()
//				.map(fillPrescription -> modelMapper.map(fillPrescription, FillPrescriptionHistory.class))
//				.collect(Collectors.toList());
//		fillPrescriptionHistories.stream().forEach(fillPrescriptionHistory -> {
//			// 1. While doing model mapping, as the id fields are matched, id of
//			// prescription entity will be added for id of prescription history entity,
//			// setting null to the id field
//			fillPrescriptionHistory.setFillPrescriptionId(fillPrescriptionHistory.getId());
//			fillPrescriptionHistory.setId(null);
//		});
//		return fillPrescriptionHistoryRepository.saveAll(fillPrescriptionHistories);
//	}

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
			List<PatientVisit> patientVisits = patientVisitService.getPatientVisitDates(
					prescriptionListRequestDTO.getPatientTrackId(), null, null, Constants.BOOLEAN_TRUE);
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
		if (Objects.isNull(prescriptionListRequestDTO.getPatientVisitId())) {
			throw new SpiceValidation(29013);
		}
		if (Objects.isNull(prescriptionListRequestDTO.getPatientTrackId())) {
			throw new SpiceValidation(29011);
		}
		Prescription prescriptionToBeDeleted = prescriptionRepository.findById(prescriptionListRequestDTO.getId())
				.orElseThrow(() -> new DataNotAcceptableException(29010));

		prescriptionToBeDeleted.setDeleted(Constants.BOOLEAN_TRUE);
		prescriptionToBeDeleted.setDiscontinuedOn(new Date());
		prescriptionToBeDeleted.setDiscontinuedReason(prescriptionListRequestDTO.getDiscontinuedReason());
		prescriptionToBeDeleted.setTenantId(prescriptionListRequestDTO.getTenantId());
		prescriptionRepository.save(prescriptionToBeDeleted);
		// patientTrackId should be included in the request
		updateMedicationPrescribed(prescriptionListRequestDTO.getPatientTrackId(), Constants.BOOLEAN_FALSE,
				Constants.BOOLEAN_TRUE);
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<FillPrescriptionResponseDTO> getFillPrescriptions(SearchRequestDTO searchRequestDTO) {
		if (Objects.isNull(searchRequestDTO.getPatientTrackId())) {
			throw new DataNotAcceptableException(10010);
		}

//		List<FillPrescriptionResponseDTO> fillPrescriptionResponseDTOs = new ArrayList<>();

		List<Prescription> prescriptions = prescriptionRepository
				.getRefillPrescriptions(searchRequestDTO.getPatientTrackId(), Constants.ZERO, Constants.BOOLEAN_FALSE);
		System.out.println("prescriptions: " + prescriptions);
		modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull()).setFullTypeMatchingRequired(true);
		List<FillPrescriptionResponseDTO> fillPrescriptionResponseDTOs = modelMapper.map(prescriptions,
				new TypeToken<List<FillPrescriptionResponseDTO>>() {
				}.getType());

		return fillPrescriptionResponseDTOs;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Prescription> updateFillPrescription(FillPrescriptionRequestDTO fillPrescriptionRequestDTO) {
		if (Objects.isNull(fillPrescriptionRequestDTO.getPatientTrackId())
				|| Objects.isNull(fillPrescriptionRequestDTO.getPatientVisitId())) {
			throw new DataNotAcceptableException(29007);
		}

		List<Prescription> prescriptions = fillPrescriptionRequestDTO.getPrescriptions();

		List<Long> prescriptionIds = prescriptions.stream().map(prescription -> prescription.getId())
				.collect(Collectors.toList());

		List<Prescription> existingPrescriptions = prescriptionRepository.getActivePrescriptions(prescriptionIds,
				Constants.BOOLEAN_FALSE);

		List<Prescription> prescriptionsToUpdate = new ArrayList<>();

		for (Prescription prescription : prescriptions) {
			Prescription existingPrescription = existingPrescriptions.stream()
					.filter(existing -> existing.getId() == prescription.getId()).findFirst()
					.orElseThrow(() -> new DataNotFoundException(29014));

			if (existingPrescription.getRemainingPrescriptionDays() < prescription.getPrescriptionFilledDays()) {
				throw new DataNotAcceptableException(29015);
			}

			int remainingPrescriptionDays = existingPrescription.getRemainingPrescriptionDays()
					- prescription.getPrescriptionFilledDays();

			existingPrescription.setPrescriptionFilledDays(prescription.getPrescriptionFilledDays());
			existingPrescription.setRemainingPrescriptionDays(remainingPrescriptionDays);
			existingPrescription.setPatientVisitId(fillPrescriptionRequestDTO.getPatientVisitId());
			existingPrescription.setTenantId(fillPrescriptionRequestDTO.getTenantId());
			existingPrescription.setPatientTrackId(fillPrescriptionRequestDTO.getPatientTrackId());
			prescriptionsToUpdate.add(existingPrescription);
		}

		prescriptions = prescriptionRepository.saveAll(prescriptionsToUpdate);

		addPrescriptionHistoryData(prescriptionsToUpdate, true);

		updateMedicationPrescribed(fillPrescriptionRequestDTO.getPatientTrackId(), Constants.BOOLEAN_FALSE,
				Constants.BOOLEAN_FALSE);

		return prescriptions;
	}

	public void updateMedicationPrescribed(Long patientTrackId, boolean isMedicationPrescribed, boolean updateDate) {
		Date nextMedicalReviewDate = null;
		Date lastAssessmentDate = null;
		if (updateDate) {
			nextMedicalReviewDate = patientTreatmentPlanService.getNextFollowUpDate(patientTrackId,
					Constants.MEDICAL_REVIEW_FREQUENCY);
			lastAssessmentDate = new Date();
		}
		if (!isMedicationPrescribed) {
			if (prescriptionRepository
					.findByPatientTrackIdAndRemainingPrescriptionDaysGreaterThan(patientTrackId, Constants.ZERO)
					.isEmpty()) {
				patientTrackerService.updateForFillPrescription(patientTrackId, Constants.BOOLEAN_TRUE,
						lastAssessmentDate, nextMedicalReviewDate);
			}
		} else {
			patientTrackerService.updateForFillPrescription(patientTrackId, Constants.BOOLEAN_TRUE, lastAssessmentDate,
					nextMedicalReviewDate);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<PrescriptionHistory> getRefillPrescriptionHistory(SearchRequestDTO searchRequestDTO) {

		List<PrescriptionHistory> prescriptionHistories = new ArrayList<>();

		if (Objects.isNull(searchRequestDTO.getPatientTrackId())
				|| Objects.isNull(searchRequestDTO.getLastRefillVisitId())) {
			throw new DataNotAcceptableException(29007);
		}
//		fillPrescriptionHistories = prescriptionHistoryRepository.getFillPrescriptionHistory(
//				searchRequestDTO.getPatientTrackId(), searchRequestDTO.getLastRefillVisitId(), Constants.ZERO);

		prescriptionHistories = prescriptionHistoryRepository.getFillPrescriptionHistory(
				searchRequestDTO.getPatientTrackId(), searchRequestDTO.getLastRefillVisitId(), Constants.ZERO);

		return prescriptionHistories;
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

	/**
	 * To convert the multipart file to file object
	 * 
	 * @param file
	 * @return File
	 */
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
