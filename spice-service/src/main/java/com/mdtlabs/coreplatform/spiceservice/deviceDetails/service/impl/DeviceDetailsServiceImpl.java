package com.mdtlabs.coreplatform.spiceservice.deviceDetails.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.contexts.UserContextHolder;
import com.mdtlabs.coreplatform.common.model.dto.UserDTO;
import com.mdtlabs.coreplatform.common.model.entity.DeviceDetails;
import com.mdtlabs.coreplatform.spiceservice.deviceDetails.repository.DeviceDetailsRepositary;
import com.mdtlabs.coreplatform.spiceservice.deviceDetails.service.DeviceDetailsService;

/**
 * This class implements the DeviceDetailsService interface and contains actual
 * business logic to perform operations on PatientVisit entity.
 *
 * @author Nandhakumar Karthikeyan
 */
@Service
public class DeviceDetailsServiceImpl implements DeviceDetailsService {
    
    @Autowired
    private DeviceDetailsRepositary deviceDetailsRepositary;

    public Map<String, Long> validateDeviceDetails(DeviceDetails deviceDetails) {
        DeviceDetails response;
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        UserDTO userDto = UserContextHolder.getUserDto();
        DeviceDetails existingDeviceDetails = deviceDetailsRepositary.findByUserIdAndTenantIdAndDeviceId(userDto.getId(), deviceDetails.getTenantId(), deviceDetails.getDeviceId());
        if (!Objects.isNull(existingDeviceDetails)){
            mapper.map(deviceDetails, existingDeviceDetails);
            existingDeviceDetails.setLastLoggedIn(new Date());
            response = deviceDetailsRepositary.save(existingDeviceDetails);
        } else {
            response = deviceDetailsRepositary.save(deviceDetails);
        }
        return Map.of("id", response.getId());
    }

}