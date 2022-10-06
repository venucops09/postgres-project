package com.mdtlabs.coreplatform.spiceservice.frequency.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.entity.spice.Frequency;
import com.mdtlabs.coreplatform.spiceservice.frequency.service.FrequencyService;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;


/**
 * This class implements the handles the RESt API calls for the frequency entity
 * and is responsible for responding with corresponding response.
 *
 * @author Niraimathi
 */
@RestController
public class FrequencyController {

    @Autowired
    FrequencyService frequencyService;

    /**
     * This method is used to add a new frequency.
     *
     * @param frequency
     * @return Frequency Entity
     * @author Niraimathi S
     */
    @RequestMapping(method = RequestMethod.POST)
    public SuccessResponse<Frequency> addFrequency(@RequestBody Frequency frequency) {
        return new SuccessResponse<>(SuccessCode.FREQUENCY_SAVE, frequencyService.addFrequency(frequency),
                HttpStatus.CREATED);
    }

    /**
     * This method is used to get a frequency based on its id.
     *
     * @param id frequency Id
     * @return Frequency Entity.
     * @author Niraimathi S
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public SuccessResponse<Frequency> getFrequency(@PathVariable("id") long id) {
        return new SuccessResponse<>(SuccessCode.GET_FREQUENCY, frequencyService.getFrequencyById(id), HttpStatus.OK);
    }

}
