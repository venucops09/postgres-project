package com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.exception.BadRequestException;
import com.mdtlabs.coreplatform.common.exception.DataNotFoundException;
import com.mdtlabs.coreplatform.common.model.entity.spice.Frequency;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTracker;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientTreatmentPlan;
import com.mdtlabs.coreplatform.spiceservice.frequency.service.FrequencyService;
import com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.repository.PatientTreatmentPlanRepository;
import com.mdtlabs.coreplatform.spiceservice.patienttreatmentplan.service.PatientTreatmentPlanService;

@Service
public class PatientTreatmentPlanServiceImpl implements PatientTreatmentPlanService {

	@Autowired
	private PatientTreatmentPlanRepository patientTreatmentPlanRepository;

	@Autowired
	private FrequencyService frequencyService;

	private ModelMapper mapper = new ModelMapper();

	public PatientTreatmentPlan addPatientTreatmentPlan(PatientTreatmentPlan treatmentPlan) {
		if (Objects.isNull(treatmentPlan)) {
			throw new BadRequestException(1000);
		}
		return patientTreatmentPlanRepository.save(treatmentPlan);
	}

	// /**
	// * {@inheritDoc}
	// */
	// public PatientTreatmentPlan getPatientTreatmentPlan(Long patientTrackId,
	// boolean isDeleted) {
	// List<PatientTreatmentPlan> treatmentPlan =
	// patientTreatmentPlanRepository.findByPatientTrackIdAndIsDeletedOrderBymodifiedAtDesc(
	// patientTrackId,
	// isDeleted);
	// return treatmentPlan.isEmpty() ? null : treatmentPlan.get(0);
	// }

	/**
	 * {@inheritDoc}
	 */
	public PatientTreatmentPlan getPatientTreatmentPlan(Long patientTrackId) {

		List<PatientTreatmentPlan> treatmentPlan = patientTreatmentPlanRepository
				.findByPatientTrackIdAndIsDeletedOrderByUpdatedAtDesc(patientTrackId, false);
		return treatmentPlan.isEmpty() ? null : treatmentPlan.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatientTreatmentPlan getPatientTreatmentPlanDetails(Long id, Long tenantId) {
		PatientTreatmentPlan treatmentPlan = patientTreatmentPlanRepository.getTreatementPlanDetails(id, tenantId);
		return treatmentPlan;
	}

	// public void getNextFollowupDate(long patientTrackId, String frquencyName) {
	// PatientTreatmentPlan patientTreatmentPlan =
	// patientTreatmentPlanRepository.getTreatementPlanDetails(patientTrackId,
	// null);
	// return get
	// }

	/**
	 * {@inheritDoc}
	 */
	public PatientTreatmentPlan getPatientTreatmentPlanDetails(Long id, String cvdRiskLevel, Long tenantId) {
		PatientTreatmentPlan treatmentPlan = patientTreatmentPlanRepository.findByPatientTrackIdAndTenantId(id,
				tenantId);
		return treatmentPlan;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Map<String, String>> createProvisionalTreatmentPlan(PatientTracker patientTracker, String cvdRiskLevel,
			Long tenantId) {
		PatientTreatmentPlan treatmentPlan = getPatientTreatmentPlanDetails(patientTracker.getId(), cvdRiskLevel,
				tenantId);
		List<Map<String, String>> treatmentPlanResponse = new ArrayList<Map<String, String>>();
		System.out.println(
				"*************************************createProvisionalTreatmentPlan,,treatmentPlan:" + treatmentPlan);
		if (Objects.isNull(treatmentPlan)) {
			List<Frequency> frequencyList = frequencyService.getFrequencyListByRiskLevel(cvdRiskLevel);
			System.out.println("--------------------------frequencyDetails-------" + frequencyList);
			treatmentPlan = new PatientTreatmentPlan();
			treatmentPlan.setTenantId(treatmentPlan.getTenantId());
			treatmentPlan.setPatientTrackId(patientTracker.getId());
			treatmentPlan.setRiskLevel(cvdRiskLevel);
			treatmentPlan.setBgCheckFrequency(Constants.BG_PROVISIONAL_FREQUENCY_NAME);
			treatmentPlan.setIsProvisional(true);
			setFrequencies(frequencyList, treatmentPlan);

//      TODO: Clinical workflows 
			treatmentPlan = patientTreatmentPlanRepository.save(treatmentPlan);
			treatmentPlanResponse = updateNextFollowupDetails(treatmentPlan, frequencyList, patientTracker);
		}
		return treatmentPlanResponse;
	}

	/**
	 * This method is used to set the frequency values in the treatmentplan data.
	 *
	 * @param frequencyList
	 * @param treatmentPlan
	 * @return treatmentPlanDurations A Map of treatment plan frequencies for fields
	 *         like medical review, bp check etc.,
	 */
	private Map<String, String> setFrequencies(List<Frequency> frequencyList, PatientTreatmentPlan treatmentPlan) {
		Map<String, String> treatmentPlanDurations = new HashMap<String, String>();
		if (!frequencyList.isEmpty()) {
			for (Frequency frequency : frequencyList) {
				switch (frequency.getType()) {
				case Constants.FREQUENCY_BP_CHECK:
					treatmentPlan.setBpCheckFrequency(frequency.getName());
					treatmentPlanDurations.put(Constants.BP_CHECK_FREQUENCY, frequency.getName());
					break;
				case Constants.FREQUENCY_BG_CHECK:
//                    treatmentPlan.setBgCheckFrequency(frequency.getFrequencyName());
					treatmentPlanDurations.put(Constants.BG_CHECK_FREQUENCY, Constants.BG_PROVISIONAL_FREQUENCY_NAME);
					break;
				case Constants.FREQUENCY_HBA1C_CHECK:
					treatmentPlan.setHba1cCheckFrequency(frequency.getName());
					treatmentPlanDurations.put(Constants.HBA1C_CHECK_FREQUENCY, frequency.getName());
					break;
				case Constants.FREQUENCY_MEDICAL_REVIEW:
					treatmentPlan.setMedicalReviewFrequency(frequency.getName());
					treatmentPlanDurations.put(Constants.MEDICAL_REVIEW_FREQUENCY, frequency.getName());
					break;
				default:
					break;
				}
			}
		}
		return treatmentPlanDurations;
	}

	/**
	 * This method is used to get next follow-up dates for next treatment plans.
	 *
	 * @param treatmentPlan
	 * @param frequencyList
	 * @param patientTracker
	 * @return Treatment plan response
	 */
	public List<Map<String, String>> updateNextFollowupDetails(PatientTreatmentPlan treatmentPlan,
			List<Frequency> frequencyList, PatientTracker patientTracker) {
//        Map<String, Date> followupDates = new HashMap<String, Date>();
		String[] frequencyNames = { Constants.FREQUENCY_MEDICAL_REVIEW, Constants.FREQUENCY_BP_CHECK,
				Constants.FREQUENCY_BG_CHECK };

		Map<String, Frequency> frequencyMap = new HashMap<>();
		if (!frequencyList.isEmpty()) {
			for (Frequency frequency : frequencyList) {
				frequencyMap.put(frequency.getType(), frequency);
			}
//        patientTracker.setNextMedicalReviewDate(followupDates.get(Constants.FREQUENCY_MEDICAL_REVIEW));
			patientTracker.setNextMedicalReviewDate(
					getTreatmentPlanFollowupDate(frequencyMap.get(Constants.FREQUENCY_MEDICAL_REVIEW)));
			patientTracker.setNextBpAssessmentDate(
					getTreatmentPlanFollowupDate(frequencyMap.get(Constants.FREQUENCY_BP_CHECK)));
			if (!treatmentPlan.getBgCheckFrequency().equals(Constants.BG_PROVISIONAL_FREQUENCY_NAME)) {
				patientTracker.setNextBgAssessmentDate(
						getTreatmentPlanFollowupDate(frequencyMap.get(Constants.FREQUENCY_BG_CHECK)));
			}
		}
		return constructTreatmentPlanResponse(frequencyNames, frequencyList);
	}

	public Date getTreatmentPlanFollowupDate(String frequencyName, String frequencyType) {
		Frequency frequency = frequencyService.getFrequencyByFrequencyNameAndType(frequencyName, frequencyType);
		return Objects.isNull(frequency) ? null : getTreatmentPlanFollowupDate(frequency);
	}

	/**
	 * Calculates the next date for the treatment plan.
	 *
	 * @param frequency
	 * @return date next treatment review date
	 * @author Rajkumar
	 */
	public Date getTreatmentPlanFollowupDate(Frequency frequency) {
		Calendar calendar = Calendar.getInstance();
		Date date = null;
		if (!Objects.isNull(frequency)) {
			System.out.println("$$$$$$$$$date" + date);
			if ("day".equals(frequency.getPeriod().toLowerCase())) {
				calendar.add(Calendar.DATE, frequency.getDuration());
				date = calendar.getTime();
			} else if ("week".equals(frequency.getPeriod().toLowerCase())) {
				calendar.add(Calendar.WEEK_OF_YEAR, frequency.getDuration());
				date = calendar.getTime();
			} else {
				calendar.add(Calendar.MONTH, frequency.getDuration());
				date = calendar.getTime();
			}
		}
		return date;
	}

	/**
	 * Constructs a treatment plan response.
	 *
	 * @param frequencyLabels
	 * @param frequencies
	 * @return treatment plan response
	 */
	public List<Map<String, String>> constructTreatmentPlanResponse(String[] frequencyLabels,
			List<Frequency> frequencies) {
		List<Map<String, String>> responseList = new ArrayList<Map<String, String>>();
		if (frequencyLabels.length == frequencies.size()) {
			for (Frequency frequency : frequencies) {
				Map<String, String> responseAsMap = new HashMap<String, String>();
				switch (frequency.getType()) {
				case Constants.FREQUENCY_MEDICAL_REVIEW:
					responseAsMap.put(Constants.LABEL, Constants.MEDICAL_REVIEW_FREQUENCY);
					responseAsMap.put(Constants.VALUE, frequency.getName());
					responseList.add(responseAsMap);
					break;
				case Constants.FREQUENCY_BP_CHECK:
					responseAsMap.put(Constants.LABEL, Constants.BP_CHECK_FREQUENCY);
					responseAsMap.put(Constants.VALUE, frequency.getName());
					responseList.add(responseAsMap);
					break;
				case Constants.FREQUENCY_BG_CHECK:
					responseAsMap.put(Constants.LABEL, Constants.BG_CHECK_FREQUENCY);
					responseAsMap.put(Constants.VALUE, Constants.BG_PROVISIONAL_FREQUENCY_NAME);
					responseList.add(responseAsMap);
					break;
				default:
					break;
				}
			}
		}
		System.out.println("\n  ____________________treatmentplan response \n\n:" + responseList);
		return responseList;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean updateTreatmentPlanData(PatientTreatmentPlan patientTreatmentPlan) {
		PatientTreatmentPlan response;
		if (Objects.isNull(patientTreatmentPlan.getId())) {
			response = patientTreatmentPlanRepository.save(patientTreatmentPlan);
		} else {
			PatientTreatmentPlan existPatientTreatmentPlan = patientTreatmentPlanRepository
					.findByIdAndIsDeleted(patientTreatmentPlan.getId(), Constants.BOOLEAN_FALSE);
			if (Objects.isNull(existPatientTreatmentPlan)) {
				throw new DataNotFoundException(13001);
			}
			mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
			mapper.map(patientTreatmentPlan, existPatientTreatmentPlan);
			response = patientTreatmentPlanRepository.save(existPatientTreatmentPlan);
		}

		return !Objects.isNull(response);
	}

	/**
	 * To get the next followup date
	 * 
	 * @param patientTrackId
	 * @param freqName
	 * @return
	 */
	public Date getNextFollowUpDate(long patientTrackId, String freqName) {
		PatientTreatmentPlan patientTreatmentPlan = patientTreatmentPlanRepository.findByPatientTrackId(patientTrackId);
		String freqType = Constants.DEFAULT;
		if (freqName.equalsIgnoreCase(Constants.FREQUENCY_MEDICAL_REVIEW)) {
			freqName = patientTreatmentPlan.getMedicalReviewFrequency();
		} else if (freqName.equalsIgnoreCase(Constants.FREQUENCY_BP_CHECK)) {
			freqName = patientTreatmentPlan.getBpCheckFrequency();
		} else if (freqName.equalsIgnoreCase(Constants.FREQUENCY_BG_CHECK)) {
			freqName = patientTreatmentPlan.getBgCheckFrequency();
		}
		return getTreatmentPlanFollowupDate(freqName, freqType);
	}
}
