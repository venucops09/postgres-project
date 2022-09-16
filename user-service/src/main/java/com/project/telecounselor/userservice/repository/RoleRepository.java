package com.project.telecounselor.userservice.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.telecounselor.model.entity.Role;

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
	 * @param status
	 * @return List of Role Entity
	 */
	@Query(value = GET_ALL_ROLES)
	public List<Role> getAllRoles(@Param("status") boolean status);

	/**
	 * <p>
	 * This method used to active or inactive the role using id (0 as inactive and 1
	 * as active).
	 * </p>
	 * 
	 * @param status
	 * @param roleId
	 * @return Role Entity
	 */
	@Query(value = UPDATE_ROLE_STATUS_BY_ID)
	public int updateRoleStatusById(@Param("status") Boolean status, @Param("roleId") long roleId);

	/**
	 * <p>
	 * This method used to get the role detail using id.
	 * </p>
	 * 
	 * @param roleId
	 * @return Role Entity
	 */
	@Query(value = GET_ROLE_BY_ID)
	public Role getRoleById(@Param("roleId") long roleId);
	
	/**
	 * <p>
	 * This method used to get the role detail using name.
	 * </p>
	 * 
	 * @param name
	 * @return Role Entity
	 */
	@Query(value = GET_ROLE_BY_NAME)
	public Role getRoleByName(@Param("name") String name);
	
	/**
	 * <p>
	 * This method used to get the roles using set of ids.
	 * </p>
	 * 
	 * @param roleIds
	 * @return Set of Role Entity
	 */
	@Query(value = GET_ROLES_BY_IDS)
	public Set<Role> getRolesByIds(@Param("roleIds") Set<Long> roleIds);

}
