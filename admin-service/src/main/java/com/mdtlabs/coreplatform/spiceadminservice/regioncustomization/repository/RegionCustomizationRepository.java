package com.mdtlabs.coreplatform.spiceadminservice.regioncustomization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.RegionCustomization;

/**
 * This Repository class contains the needed customized functions for region
 * customization.
 * 
 * @author Jeyaharini T A
 *
 */
@Repository
public interface RegionCustomizationRepository extends JpaRepository<RegionCustomization, Long> {

	public static final String GET_COUNTRY_CUSOMIZATION_WITH_CONDITIONS = "SELECT regioncustomization FROM RegionCustomization as regioncustomization "
			+ " WHERE (regioncustomization.countryId = :countryId) AND (:category IS null OR regioncustomization.category = :category)"
			+ " AND (:type IS null OR upper(regioncustomization.type) = upper(:type)) AND regioncustomization.isDeleted= :isDeleted";

	/**
	 * Gets a Region customization by Id.
	 * 
	 * @param id
	 * @return RegionCustomization entity.
	 * @author Niraimathi S
	 */
	public RegionCustomization findByIdAndIsDeleted(Long id, Boolean isDeleted);

	/**
	 * To get a Region customization details with conditions
	 * 
	 * @param countryId
	 * @param category
	 * @param type
	 * @return RegionCustomization entity
	 * @author Jeyaharini T A
	 */
	@Query(value = GET_COUNTRY_CUSOMIZATION_WITH_CONDITIONS)
	public RegionCustomization findByCountryIdAndCategoryAndType(@Param("countryId") Long countryId,
			@Param("category") String category, @Param("type") String type, @Param("isDeleted") boolean isDeleted);

}
