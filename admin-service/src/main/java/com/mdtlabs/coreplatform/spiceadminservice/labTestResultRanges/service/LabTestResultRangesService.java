package com.mdtlabs.coreplatform.spiceadminservice.labTestResultRanges.service;

import java.util.List;

import com.mdtlabs.coreplatform.common.model.dto.spice.LabTestResultRangeRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTestResultRange;

/**
 * This interface maintains the CRUD operation for lab test result range
 * 
 * @author Rajkumar
 *
 */
public interface LabTestResultRangesService {

	/**
	 * To add the list of LabTestResultRanges
	 * 
	 * @param labTestResultRangeDTO
	 * @return List of added LabTestResultRange
	 * @author Rajkumar
	 */
	public List<LabTestResultRange> addLabTestResultRanges(LabTestResultRangeRequestDTO labTestResultRangeDTO);

	/**
	 * To update the list of LabTestResultRanges by it's id
	 * 
	 * @param labTestResultRanges
	 * @return List of updated LabTestResultRange
	 * @author Rajkumar
	 */
	public List<LabTestResultRange> updateLabTestResultRanges(
			LabTestResultRangeRequestDTO labTestResultRangeRequestDTO);

	/**
	 * To remove the list of LabTestResultRange by it's id
	 * 
	 * @param id
	 * @return boolean - removal status
	 * @author Rajkumar
	 */
	public boolean removeLabTestResultRange(long id);

	/**
	 * To list the LabTestResultRange by lab test result id
	 * 
	 * @param labTestResultId
	 * @return List of LabTestResultRangeDTO
	 * @author Rajkumar
	 */
	public List<LabTestResultRange> getLabTestResultRange(long labTestResultId);

}
