package com.mdtlabs.coreplatform.spiceservice.customizedmodules.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.model.entity.spice.CustomizedModule;
import com.mdtlabs.coreplatform.spiceservice.customizedmodules.repository.CustomizedModuleRepository;
import com.mdtlabs.coreplatform.spiceservice.customizedmodules.service.CustomizedModulesService;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains business logic for CustomizedModules entity.
 * 
 * @author Niraimathi S
 */
@Service
public class CustomizedModulesServiceImpl implements CustomizedModulesService {
    @Autowired
    private CustomizedModuleRepository customizedModuleRepository;

    /**
     * {@inheritDoc}
     */
    public void createCustomizedModules(List<Map<String, Object>> modules, String type, Long patientTrackId) {
        System.out.println("*****************************modules" + modules);
        List<CustomizedModule> updatedModules = modules.stream().map(module -> {
            CustomizedModule customizedModule = new CustomizedModule();
            customizedModule.setModuleValue(module);
            customizedModule.setScreenType(type);
            customizedModule.setPatientTrackId(patientTrackId);
            customizedModule.setClinicalworkflowId((Long.parseLong(module.get("_id").toString())));
            return customizedModule;
        }).filter(obj -> true).collect(Collectors.toList());
        System.out.println("_____________________________updatedModules" + updatedModules);
        customizedModuleRepository.saveAll(updatedModules);
//        List<CustomizedModule> updatedModules = new ArrayList<>();
    }
}
