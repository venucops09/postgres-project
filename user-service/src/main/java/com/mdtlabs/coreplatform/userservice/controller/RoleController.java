package com.mdtlabs.coreplatform.userservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.userservice.message.SuccessCode;
import com.mdtlabs.coreplatform.userservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.userservice.service.RoleService;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.model.entity.Role;

import io.swagger.annotations.Api;

/**
 * <p>
 * Role Controller used to perform any action in the role module like read and
 * write.
 * </p>
 * 
 * @author VigneshKumar created on Jan 30, 2022
 */
@RestController
@RequestMapping(value = "/role")
@Api(value = Constants.MASTER_DATA, produces = Constants.APPLICATION_JSON)
public class RoleController {

	@Autowired
	private RoleService roleService;

	/**
	 * <p>
	 * Add new role for some action privileges.
	 * </p>
	 * 
	 * @param role - role information to be added
	 * @return Role - response of adding the role
	 */
	@PostMapping
	public SuccessResponse<Role> addRole(@RequestBody Role role) {
		Role newRole = roleService.addRole(role);
		return new SuccessResponse<>((null == newRole) ? SuccessCode.ROLE_SAVE_ERROR : SuccessCode.ROLE_SAVE, newRole, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Retrieve all the active roles detail list.
	 * </p>
	 * 
	 * @return List<Role> - List of Role Entity
	 */
	@GetMapping
	public SuccessResponse<List<Role>> getAllRoles() {
		List<Role> roleList = roleService.getAllRoles();
		return new SuccessResponse((roleList.isEmpty()) ? SuccessCode.GET_ROLES_ERROR : SuccessCode.GET_ROLES, roleList, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Update Role details like change the name.
	 * </p>
	 * 
	 * @param role - role information to be updated
	 * @return Role - response of the updated role
	 */
	@PutMapping
	public SuccessResponse<Role> updateRole(@RequestBody Role role) {
		Role updatedRole = roleService.updateRole(role);
		return new SuccessResponse<>((null == updatedRole) ? SuccessCode.ROLE_UPDATE_ERROR : SuccessCode.ROLE_UPDATE, updatedRole, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Inactive the role by id.
	 * </p>
	 * 
	 * @param roleId - the id of the role which has be deleted
	 * @return Role - response count of delete
	 */
	@DeleteMapping
	public SuccessResponse<Role> deleteRoleById(@PathVariable(value = Constants.ROLE_ID_PARAM) long roleId) {
		int role = roleService.deleteRoleById(roleId);
		return new SuccessResponse<>((role == Constants.ONE) ? SuccessCode.ROLE_DELETE : SuccessCode.ROLE_DELETE_ERROR, roleService.deleteRoleById(roleId), HttpStatus.OK);
	}

	/**
	 * This method is used to get role information by role id
	 * 
	 * @param roleId - the id of the role
	 * @return Role - role information
	 */
	@GetMapping(value = "/{id}")
	public SuccessResponse<Role> getRoleById(@PathVariable(value = Constants.ID) long roleId) {
		return new SuccessResponse<>(SuccessCode.GET_ROLE, roleService.getRoleById(roleId), HttpStatus.OK);
	}

	/**
	 * This method is used to get role information by role name
	 * 
	 * @param name - the name of the role
	 * @return Role - role information
	 */
	@GetMapping(value = "/{name}")
	public SuccessResponse<Role> getRoleByName(@PathVariable(value = Constants.NAME) String name) {
		return new SuccessResponse<>(SuccessCode.GET_ROLE, roleService.getRoleByName(name), HttpStatus.OK);
	}

}
