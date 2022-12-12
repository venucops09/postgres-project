package com.mdtlabs.coreplatform.spiceadminservice.data.repository;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.Subcounty;

/**
 * This interface provides database connection for SubCounty entity.
 *
 * @author Karthick M
 */
@Repository
public interface SubCountyRepository extends JpaRepository<Subcounty, Long> {

	public static final String GET_ALL_SUBCOUNTIES = "select subcounty from Subcounty as subcounty where subcounty.countryId=:countryId AND subcounty.countyId=:countyId AND subcounty.isDeleted=false";

	/**
	 * Finds the subcounty based on name
	 *
	 * @param name
	 * @return SubCounty
	 */
	public Subcounty findByName(String name);

	/**
	 * Finds the subcounty based on id
	 *
	 * @param id
	 * @param isDeleted
	 * @return SubCounty
	 */
	public Subcounty findByIdAndIsDeleted(long id, boolean isDeleted);

	/**
	 * Finds the subcounty based on id
	 *
	 * @param id
	 * @param isActive
	 * @return SubCounty
	 */
	public Subcounty findByIdAndIsActive(long id, boolean isActive);

	/**
	 * Finds the subcounty based on countryId and countyId and name
	 *
	 * @param countryId
	 * @param countyId
	 * @param name
	 * @return SubCounty
	 */
	public Subcounty findByCountryIdAndCountyIdAndName(@NotNull long countryId, @NotNull long countyId,
			@NotNull String name);

	/**
	 * Finds the subcounty based on countryId and countyId
	 *
	 * @param countryId
	 * @param countyId
	 * @param isDeleted
	 * @return List<SubCounty>
	 */
	public List<Subcounty> findByCountryIdAndCountyIdAndIsDeleted(long countryId, @NotNull long countyId,
			boolean isDeleted);

	/**
	 * Gets all subcounty entities.
	 *
	 * @param countryId
	 * @param countyId
	 * @return List of Subcounty entities.
	 * @author Niraimathi S
	 */
	@Query(value = GET_ALL_SUBCOUNTIES)
	public List<Subcounty> getAllSubCounty(@Param("countryId") long countryId, @Param("countyId") long countyId);

	/**
	 *
	 * @param countryId
	 * @return
	 */
	public List<Subcounty> findByCountryId(Long countryId);

}
