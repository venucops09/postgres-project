package com.project.telecounselor.authservice.repository;

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
 * @author VigneshKumar created on Aug 26, 2022
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long>, PagingAndSortingRepository<Role, Long> {

	public static final String GET_ALL_ROLES = "select role from Role as role where role.isActive =:status ";

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
}
