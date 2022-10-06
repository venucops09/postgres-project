package com.mdtlabs.coreplatform.spiceservice;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.mdtlabs.coreplatform.common.model.dto.spice.OtherMedicationDTO;
import com.mdtlabs.coreplatform.common.model.dto.spice.SearchRequestDTO;
import com.mdtlabs.coreplatform.common.model.entity.spice.LabTest;


//@FeignClient(value = "AdminServiceFeign", url = "http://192.168.178.174/admin-service")
@FeignClient(name = "admin", path = "/admin-service")
public interface ApiInterface {

	@GetMapping("/medication/other-medication/{countryId}")
	public ResponseEntity<OtherMedicationDTO> getOtherMedication(@PathVariable long countryId);

	@PostMapping("/labtest/patient-labtest/get-by-name")
	public ResponseEntity<LabTest> getLabTestByName(@RequestBody SearchRequestDTO searchRequestDTO);

	@PostMapping("/labtest/patient-labtest/get-list-by-ids")
	public ResponseEntity<List<LabTest>> getLabTestsByIds(@RequestBody Set<Long> labTestIds);

	@GetMapping("/labtest/labtest-result/{id}")
	public ResponseEntity<Map> getLabTestResultsByLabTestId(@PathVariable long id);

}
