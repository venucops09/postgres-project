package com.mdtlabs.coreplatform.spiceservice.deviceDetails.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.DeviceDetails;


@Repository
public interface DeviceDetailsRepositary extends JpaRepository<DeviceDetails, Long> {	
    public DeviceDetails findByUserIdAndTenantIdAndDeviceId(Long userId, Long tenantId, String deviceId); 
}
