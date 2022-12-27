package com.mdtlabs.coreplatform.spiceservice.customizedmodules.service;

import java.util.List;
import java.util.Map;

/**
 * This interface is responsible for performing actions in CustomizedModules Entity.
 * 
 * @author Rajkumar
 */
public interface CustomizedModulesService {
    /**
     * Creates customized modules.
     *
     * @param modules        customized modules with dynamic fields and its values.
     * @param type           type of workflow like Screening, Enrollment or Assessment
     * @param patientTrackId patientTrack Id
     * @author Rajkumar
     */
    public void createCustomizedModules(List<Map<String, Object>> modules, String type, Long patientTrackId);
}
