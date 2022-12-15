package com.mdtlabs.coreplatform.userservice.service;

import java.util.List;
import java.util.Set;

import com.mdtlabs.coreplatform.common.model.entity.Role;


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
	 * @param role - role to be added
	 * @return Role - added role information
	 */
	Role addRole(Role role);

	/**
	 * <p>
	 * This method used to retrieve all the active role.
	 * </p>
	 * 
	 * @return List<Role> - list of Role Entity
	 */
	List<Role> getAllRoles();

	/**
	 * <p>
	 * This method used to update a role details.
	 * </p>
	 * 
	 * @param role - updating role
	 * @return Role - update role entity
	 */
	Role updateRole(Role role);

	/**
	 * <p>
	 * This method used to inactive a role using id.
	 * </p>
	 * 
	 * @param roleId - role id to be deleted
	 * @return int - value indicating the role delete
	 */
	int deleteRoleById(long roleId);

	/**
	 * <p>
	 * This method used to get a role detail by id.
	 * </p>
	 * 
	 * @param roleId - role id
	 * @return Role - role entity
	 */
	Role getRoleById(long roleId);

	/**
	 * <p>
	 * This method used to get a role detail by name.
	 * </p>
	 * 
	 * @param name - role name
	 * @return Role - role entity
	 */
	Role getRoleByName(String name);

	/**
	 * To get list of roles based on list of role names
	 * 
	 * @param roles - list of role names
	 * @return Set<Role> - List of Role entity
	 */
	Set<Role> getRolesByName(List<String> roles);

}
