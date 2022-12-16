package com.mdtlabs.coreplatform.spiceservice.deviceDetails.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.common.model.entity.DeviceDetails;
import com.mdtlabs.coreplatform.spiceservice.deviceDetails.service.DeviceDetailsService;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessCode;
import com.mdtlabs.coreplatform.spiceservice.message.SuccessResponse;

import io.swagger.annotations.Api;

/**
 * This class is a controller class to perform operation on DeviceDetails entity.
 * 
 * @author Nandhakumar Karthikeyan
 * 
 */
@RestController
@RequestMapping(value = "/devicedetails")
@Validated
@Api(basePath = "/devicedetails", value = "master_data", description = "Device Releated Data", produces = "application/json")
public class DeviceDetailsController {

    @Autowired
    private DeviceDetailsService deviceDetailsService;

    /**
     * This method is used to validate device details.
     *
     * @param deviceDetails
     * @return deviseDetails Entity.
     * @author Nandhakumar Karthikeyan
     */
    @RequestMapping(method = RequestMethod.POST)
    public SuccessResponse<Map<String, Long>> validateDeviceDetails(@RequestBody DeviceDetails deviceDetails) {
        return new SuccessResponse<Map<String, Long>>(SuccessCode.DEVICE_DETAILS_SAVE,
            deviceDetailsService.validateDeviceDetails(deviceDetails), HttpStatus.CREATED);
    }

}
