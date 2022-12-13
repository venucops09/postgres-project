package com.mdtlabs.coreplatform.spiceservice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.mdtlabs.coreplatform.common.model.dto.spice.SmsDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.OutBoundSMS;

@FeignClient(name = "notification", path = "/notification-service")
public interface NotificationApiInterface {

	@PostMapping("/sms/save-outboundsms")
	public ResponseEntity<OutBoundSMS> saveOutBoundSMS(@RequestHeader("Authorization") String token,
			@RequestBody SmsDTO smsData);

}
