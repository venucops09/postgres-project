package com.mdtlabs.coreplatform.spiceservice.customizedmodules.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mdtlabs.coreplatform.common.model.entity.spice.CustomizedModule;

/**
 * This interface is used for maintaining connection between server and database for CustomizedModules Entity.
 *
 * @author Rajkumar
 */
public interface CustomizedModuleRepository extends JpaRepository<CustomizedModule, Long> {
}
