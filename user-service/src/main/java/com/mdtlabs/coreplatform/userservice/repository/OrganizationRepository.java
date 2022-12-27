package com.mdtlabs.coreplatform.userservice.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.model.entity.Organization;

/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the organization module action in
 * database. In query annotation (nativeQuery = true) the below query perform
 * like SQL. Otherwise its perform like HQL default value for nativeQuery FALSE
 * </p>
 * 
 * @author Rajkumar created on Jan 30, 2022
 */
@Repository
public interface OrganizationRepository
		extends JpaRepository<Organization, Long>, PagingAndSortingRepository<Organization, Long> {

	public static final String GET_ALL_ORGANIZATIONS = "select organization from Organization as organization where organization.isActive =:status ";
	public static final String UPDATE_ORGANIZATION_STATUS_BY_ID = "update Organization as organization set organization.isActive =:status where organization.id =:id";
	public static final String GET_ORGANIZATIONS_BY_IDS = "select organization from Organization as organization where organization.id in (:organizationIds)";
	public static final String GET_ORGANIZATION_BY_ID = "select organization from Organization as organization where organization.id =:id ";
	public static final String GET_ORGANIZATION_BY_NAME = "select organization from Organization as organization where organization.name =:name ";
	public static final String ACTIVE_INACTIVE_ORG = "UPDATE Organization SET isActive = :isActive WHERE id = :id";
	public static final String ACTIVE_INACTIVE_CHILD_ORG = "UPDATE Organization SET isActive = :isActive WHERE id IN (:childOrgIds) ";
	public static final String GET_CHILD_ORG_COUNT = "select count(o2) as siteCount,(select count(id) from organization"
			+ " o where o.parent_organization_id = :tenantId) as OUCount from organization o2 where "
			+ "o2.parent_organization_id in (select id from organization o where o.parent_organization_id = :tenantId)";
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
	 * This method used to active or inactive the organization using id (0 as
	 * inactive and 1 as active).
	 * </p>
	 * 
	 * @param status         - state of the organization as true or false
	 * @param organizationId - organization id
	 * @return int response of organization update
	 */
	@Query(value = UPDATE_ORGANIZATION_STATUS_BY_ID)
	public int updateOrganizationStatusById(@Param(FieldConstants.STATUS) Boolean status,
			@Param(FieldConstants.ID) long id);

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

	/**
	 * Gets organization using name and isDeleted fields.
	 * 
	 * @param name - organization name
	 * @return Organization - organization entity
	 */
	public Organization findByNameIgnoreCaseAndIsDeletedFalse(String name);

//	@Query(value = GET_ORGANISATION_IDS_BY_USER)
//	public List<Long> getUserTenants(@Param("userId") Long userId);

	/**
	 * Finds organization by its parent organization Id.
	 * 
	 * @param tenantId - parentOrganization Id
	 * @return List<Organization> - List of organization entities.
	 */
	List<Organization> findByParentOrganizationId(long parentOrganizationId);

	/**
	 * To activate or inactivate organization.
	 * 
	 * @param id       organization Id
	 * @param isActive activate status
	 * @return int - number of affected rows.
	 */
	@Query(value = ACTIVE_INACTIVE_ORG)
	@Modifying
	@Transactional
	int activateInactivateOrganizations(@Param("id") long id, @Param("isActive") boolean isActive);

	/**
	 * Activate or inactivate child organizations using IDs of child organizations.
	 * 
	 * @param childOrgIds child organization IDs
	 * @param isActive    Activation status
	 * @return Number of affected rows
	 */
	@Query(value = ACTIVE_INACTIVE_CHILD_ORG)
	@Modifying
	@Transactional
	int activateInactivateChildOrganizations(@Param("childOrgIds") List<Long> childOrgIds,
			@Param("isActive") boolean isActive);

	/**
	 * Finds organizations by parent organization id list.
	 * 
	 * @param childOrgIds child organization Id list
	 * @return List<Organization> list of Organization entity.
	 */
	List<Organization> findByParentOrganizationIdIn(List<Long> childOrgIds);

	/**
	 * Gets child organizations count for an organization.
	 * 
	 * @param tenantId organization Id
	 * @return Map<String, Integer> - Collection of child organization counts.
	 */
	@Query(value = GET_CHILD_ORG_COUNT, nativeQuery = true)
	Map<String, Integer> getChildOrganizationCount(@Param("tenantId") Long tenantId);

	/**
	 * To get Organization based on id and isDeleted fields.
	 * 
	 * @param id organization id
	 * @return Organization entity
	 */
	Organization findByIdAndIsDeletedFalse(Long id);
	
	/**
	 * <p>
	 * To get list of organizations using list of ids.
	 * </p>
	 * @param roleNames - list of organizations ids
	 * @return Set<Organization> - Set of Organization Entities
	 */
	public Set<Organization> findByIsDeletedFalseAndIsActiveTrueAndIdIn(List<Long> organizationIds);

}
