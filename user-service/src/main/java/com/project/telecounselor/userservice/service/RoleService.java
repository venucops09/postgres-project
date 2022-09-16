package com.project.telecounselor.userservice.service;

import java.util.List;
import java.util.Set;

import com.project.telecounselor.model.entity.Role;

/**
 * <p>
 * This an interface class for role module you can implemented this class in any
 * class.
 * </p>
 * 
 * @author VigneshKumar created on Jun 30, 2022
 */
public interface RoleService {

	/**
	 * <p>
	 * This method used to add a role detail.
	 * </p>
	 * 
	 * @param role
	 * @return Role Entity
	 */
	Role addRole(Role role);

	/**
	 * <p>
	 * This method used to retrieve all the active role.
	 * </p>
	 * 
	 * @return List of Role Entity
	 */
	List<Role> getAllRoles();

	/**
	 * <p>
	 * This method used to update a role details.
	 * </p>
	 * 
	 * @param role
	 * @return Role Entity
	 */
	Role updateRole(Role role);

	/**
	 * <p>
	 * This method used to inactive a role using id.
	 * </p>
	 * 
	 * @param roleId
	 * @return Success Count
	 */
	int deleteRoleById(long roleId);

	/**
	 * <p>
	 * This method used to get a role detail by id.
	 * </p>
	 * 
	 * @param roleId
	 * @return Role Entity
	 */
	Role getRoleById(long roleId);
	
	/**
	 * <p>
	 * This method used to get a role detail by name.
	 * </p>
	 * 
	 * @param name
	 * @return Role Entity
	 */
	Role getRoleByName(String name);

	/**
	 * <p>
	 * This method used to get the roles list using set of role ids.
	 * </p>
	 * 
	 * @param roleIds
	 * @return Set of Role Entity
	 */
	Set<Role> getRolesByIds(Set<Long> roleIds);

}
