package com.mdtlabs.coreplatform.userservice.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.model.entity.Organization;


/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the organization module action in database.
 * In query annotation (nativeQuery = true) the below query perform like SQL.
 * Otherwise its perform like HQL default value for nativeQuery FALSE
 * </p>
 * 
 * @author VigneshKumar created on Jan 30, 2022
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long>, PagingAndSortingRepository<Organization, Long> {

	public static final String GET_ALL_ORGANIZATIONS = "select organization from Organization as organization where organization.isActive =:status ";
	public static final String UPDATE_ORGANIZATION_STATUS_BY_ID = "update Organization as organization set organization.isActive =:status where organization.id =:id";
	public static final String GET_ORGANIZATIONS_BY_IDS = "select organization from Organization as organization where organization.id in (:organizationIds)";
	public static final String GET_ORGANIZATION_BY_ID = "select organization from Organization as organization where organization.id =:id ";
	public static final String GET_ORGANIZATION_BY_NAME = "select organization from Organization as organization where organization.name =:name ";
//	public static final String GET_ORGANISATION_IDS_BY_USER = "select uo.organization_id from user_organization as uo where uo.userId=:userId";
	
	
	/**
	 * <p>
	 * This method get all the active organization details from the database.
	 * </p>
	 * 
	 * @param status - state of the organization as true or false
	 * @return List<Organization> - List of Organization Entity
	 */
	@Query(value = GET_ALL_ORGANIZATIONS)
	public List<Organization> getAllOrganizations(@Param(FieldConstants.STATUS) boolean status);

	/**
	 * <p>
	 * This method used to active or inactive the organization using id (0 as inactive and 1
	 * as active).
	 * </p>
	 * 
	 * @param status - state of the organization as true or false
	 * @param organizationId - organization id
	 * @return int response of organization update
	 */
	@Query(value = UPDATE_ORGANIZATION_STATUS_BY_ID)
	public int updateOrganizationStatusById(@Param(FieldConstants.STATUS) Boolean status, @Param(FieldConstants.ID) long id);

	/**
	 * <p>
	 * This method used to get the organization detail using id.
	 * </p>
	 * 
	 * @param organizationId - organization id
	 * @return Organization Entity
	 */
	@Query(value = GET_ORGANIZATION_BY_ID)
	public Organization getOrganizationById(@Param(FieldConstants.ID) long id);

	/**
	 * <p>
	 * This method used to get the organization detail using name.
	 * </p>
	 * 
	 * @param name - name of the organization
	 * @return Organization Entity
	 */
	@Query(value = GET_ORGANIZATION_BY_NAME)
	public Organization getOrganizationByName(@Param(FieldConstants.NAME) String name);
	
	
//	@Query(value = GET_ORGANISATION_IDS_BY_USER)
//	public List<Long> getUserTenants(@Param("userId") Long userId);

}
