package com.mdtlabs.coreplatform.spiceadminservice.operatingunit.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.Operatingunit;


@Repository
public interface OperatingUnitRepository extends JpaRepository<Operatingunit, Long> {

}
