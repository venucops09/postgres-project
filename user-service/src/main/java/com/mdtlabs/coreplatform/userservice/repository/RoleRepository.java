package com.mdtlabs.coreplatform.userservice.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.model.entity.Role;


/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the role module action in database.
 * In query annotation (nativeQuery = true) the below query perform like SQL.
 * Otherwise its perform like HQL default value for nativeQuery FALSE
 * </p>
 * 
 * @author VigneshKumar created on Jan 30, 2022
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, PagingAndSortingRepository<Role, Long> {

	public static final String GET_ALL_ROLES = "select role from Role as role where role.isActive =:status ";
	public static final String UPDATE_ROLE_STATUS_BY_ID = "update Role as role set role.isActive =:status where role.id =:roleId";
	public static final String GET_ROLES_BY_IDS = "select role from Role as role where role.id in (:roleIds)";
	public static final String GET_ROLE_BY_ID = "select role from Role as role where role.id =:roleId ";
	public static final String GET_ROLE_BY_NAME = "select role from Role as role where role.name =:name ";

	/**
	 * <p>
	 * This method get all the active role details from the database.
	 * </p>
	 * 
	 * @param status - state of the role as true or false
	 * @return List<Role> - List of Role Entity
	 */
	@Query(value = GET_ALL_ROLES)
	public List<Role> getAllRoles(@Param(FieldConstants.STATUS) boolean status);

	/**
	 * <p>
	 * This method used to active or inactive the role using id (0 as inactive and 1
	 * as active).
	 * </p>
	 * 
	 * @param status - state of the role as true or false
	 * @param roleId - role id
	 * @return int response of role update
	 */
	@Query(value = UPDATE_ROLE_STATUS_BY_ID)
	public int updateRoleStatusById(@Param(FieldConstants.STATUS) Boolean status, @Param(Constants.ROLE_ID_PARAM) long roleId);

	/**
	 * <p>
	 * This method used to get the role detail using id.
	 * </p>
	 * 
	 * @param roleId - role id
	 * @return Role Entity
	 */
	@Query(value = GET_ROLE_BY_ID)
	public Role getRoleById(@Param(Constants.ROLE_ID_PARAM) long roleId);

	/**
	 * <p>
	 * This method used to get the role detail using name.
	 * </p>
	 * 
	 * @param name - name of the role
	 * @return Role Entity
	 */
	@Query(value = GET_ROLE_BY_NAME)
	public Role getRoleByName(@Param(FieldConstants.NAME) String name);

	/**
	 * To get list of roles using list of role name.
	 * @param roleNames - list of role names
	 * @return Set<Role> - Set of Role Entities
	 */
	public Set<Role> findByIsDeletedFalseAndIsActiveTrueAndNameIn(List<String> roleNames);

}
