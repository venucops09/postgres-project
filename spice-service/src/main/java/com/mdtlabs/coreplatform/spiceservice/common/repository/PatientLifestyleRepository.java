package com.mdtlabs.coreplatform.spiceservice.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.Lifestyle;
import com.mdtlabs.coreplatform.common.model.entity.spice.PatientLifestyle;

/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the PatientLifestyle module action
 * in database. In query annotation (nativeQuery = true) the below query perform
 * like SQL. Otherwise its perform like HQL default value for nativeQuery FALSE
 * </p>
 * 
 * @author Rajkumar
 */
@Repository
public interface PatientLifestyleRepository extends JpaRepository<PatientLifestyle, Long> {

	PatientLifestyle findFirstByPatientTrackIdOrderByUpdatedAtDesc(Long id);

}
