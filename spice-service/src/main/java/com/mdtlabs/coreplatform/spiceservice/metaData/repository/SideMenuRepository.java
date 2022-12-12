package com.mdtlabs.coreplatform.spiceservice.metaData.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.model.entity.spice.SideMenu;
import com.mdtlabs.coreplatform.common.repository.GenericRepository;

/**
 * This interface is the Repository interface for Entity SideMenu and is
 * responsible for database operations for this entity.
 * 
 * @author ubuntu
 *
 */
@Repository
public interface SideMenuRepository extends GenericRepository<SideMenu> {

	/**
	 * Gets Lisit of side menus based on list of role names.
	 * 
	 * @param userRoles list of user role names
	 * @return List of SideMenu entity.
	 * @author Niraimathi S
	 */
	List<SideMenu> findByRoleNameIn(List<String> userRoles);

}
