package com.project.telecounselor.userservice.service.impl;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import com.project.telecounselor.model.entity.Role;
import com.project.telecounselor.service.AuditService;
import com.project.telecounselor.userservice.repository.CustomRepositoryImpl;
import com.project.telecounselor.userservice.repository.RoleRepository;
import com.project.telecounselor.userservice.service.RoleService;

/**
 * <p>
 * This service class contain all the business logic for role module and perform
 * all the role operation here.
 * </p>
 * 
 * @author VigneshKumar created on Jun 30, 2022
 */
@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private CustomRepositoryImpl customRepositoryImpl;

	/**
	 * {@inheritDoc}
	 */
	public Role addRole(Role role) {
		return customRepositoryImpl.saveRole(role);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Role> getAllRoles() {
		return roleRepository.getAllRoles(Boolean.TRUE);
	}

	/**
	 * {@inheritDoc}
	 */
	public Role updateRole(Role role) {
		Role existingRole = roleRepository.getRoleById(role.getId());
		return customRepositoryImpl.updateRole(role, existingRole);
	}

	/**
	 * {@inheritDoc}
	 */
	@Modifying
	@Transactional
	public int deleteRoleById(long roleId) {
		Role role = roleRepository.getRoleById(roleId);
		return customRepositoryImpl.deleteRoleById(roleId, role);
	}

	/**
	 * {@inheritDoc}
	 */
	public Role getRoleById(long roleId) {
		return roleRepository.getRoleById(roleId);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Role getRoleByName(String name) {
		return roleRepository.getRoleByName(name);
	}


	/**
	 * {@inheritDoc}
	 */
	public Set<Role> getRolesByIds(Set<Long> roleIds) {
		return roleRepository.getRolesByIds(roleIds);
	}
}
