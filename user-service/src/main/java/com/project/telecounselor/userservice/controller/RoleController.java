package com.project.telecounselor.userservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.project.telecounselor.common.Constants;
import com.project.telecounselor.common.util.TokenParse;
import com.project.telecounselor.model.entity.Role;
import com.project.telecounselor.model.entity.User;
import com.project.telecounselor.userservice.service.RoleService;
import com.project.telecounselor.userservice.message.SuccessCode;
import com.project.telecounselor.userservice.message.SuccessResponse;

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
@Api(basePath = "/role", value = "master_data", description = "Role related APIs", produces = "application/json")
public class RoleController {

	@Autowired
	private RoleService roleService;

	/**
	 * <p>
	 * Add new role for some action privileges.
	 * </p>
	 * 
	 * @param role
	 * @return Role Entity
	 */
	@RequestMapping(method = RequestMethod.POST)
	@TokenParse
	public SuccessResponse<Role> addRole(@RequestBody Role role) {
		return new SuccessResponse<Role>(SuccessCode.ROLE_SAVE, roleService.addRole(role), HttpStatus.OK);
	}

	/**
	 * <p>
	 * Retrieve all the active roles detail list.
	 * </p>
	 * 
	 * @return List of Role Entity
	 */
	@RequestMapping(method = RequestMethod.GET)
	public SuccessResponse<List<Role>> getAllRoles() {
		return new SuccessResponse<List<Role>>(SuccessCode.GET_ROLES, roleService.getAllRoles(), HttpStatus.OK);
	}

	/**
	 * <p>
	 * Update Role details like change the name.
	 * </p>
	 * 
	 * @param role
	 * @return Role Entity
	 */
	@RequestMapping(method = RequestMethod.PUT)
	@TokenParse
	public SuccessResponse<Role> updateRole(@RequestBody Role role) {
		return new SuccessResponse<Role>(SuccessCode.ROLE_UPDATE, roleService.updateRole(role), HttpStatus.OK);
	}

	/**
	 * <p>
	 * Inactive the role by id.
	 * </p>
	 * 
	 * @param roleId
	 * @return Success Count
	 */
	@RequestMapping(method = RequestMethod.DELETE)
	@TokenParse
	public SuccessResponse<Role> deleteRoleById(@PathVariable(value = Constants.ROLEID) long roleId) {
		return new SuccessResponse<Role>(SuccessCode.ROLE_DELETE, roleService.deleteRoleById(roleId), HttpStatus.OK);
	}

	/**
	 * <p>
	 * Retrieve Role detail using their id.
	 * </p>
	 * 
	 * @return User Entity
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<User> getRoleById(@PathVariable(value = Constants.ID) long roleId) {
		return new SuccessResponse<User>(SuccessCode.GET_ROLE, roleService.getRoleById(roleId), HttpStatus.OK);
	}
	
	/**
	 * <p>
	 * Retrieve Role detail using their id.
	 * </p>
	 * 
	 * @return User Entity
	 */
	@RequestMapping(value = "/get-role/{name}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<Role> getRoleByName(@PathVariable(value = Constants.NAME) String name) {
		return new SuccessResponse<Role>(SuccessCode.GET_ROLE, roleService.getRoleByName(name), HttpStatus.OK);
	}

}
