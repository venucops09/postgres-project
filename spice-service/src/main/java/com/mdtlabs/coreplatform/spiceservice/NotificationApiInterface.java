package com.mdtlabs.coreplatform.spiceservice;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.mdtlabs.coreplatform.common.model.dto.spice.SmsDTO;

@FeignClient(name = "notification")
public interface NotificationApiInterface {

	@PostMapping("/sms/save-outboundsms")
	public ResponseEntity<Boolean> saveOutBoundSMS(@RequestHeader("Authorization") String token,
			@RequestBody List<SmsDTO> smsData);

	@GetMapping("/sms/get-sms-template-values/{templateType}")
	public ResponseEntity<Map<String, Object>> getSMSTemplateValues(@RequestHeader("Authorization") String token,
			@PathVariable String templateType);

}
