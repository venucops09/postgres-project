package com.mdtlabs.coreplatform.userservice.service.impl;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import com.mdtlabs.coreplatform.common.model.entity.Role;
import com.mdtlabs.coreplatform.userservice.repository.RoleRepository;
import com.mdtlabs.coreplatform.userservice.service.RoleService;

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


	/**
	 * {@inheritDoc}
	 */
	public Role addRole(Role role) {
		return roleRepository.save(role);
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
		return roleRepository.save(role);
	}

	/**
	 * {@inheritDoc}
	 */
	@Modifying
	@Transactional
	public int deleteRoleById(long roleId) {
		return roleRepository.updateRoleStatusById(Boolean.FALSE, roleId);
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
	public Set<Role> getRolesByName(List<String> roleNames) {
		return roleRepository.findByIsDeletedFalseAndIsActiveTrueAndNameIn(roleNames);
	}


}
