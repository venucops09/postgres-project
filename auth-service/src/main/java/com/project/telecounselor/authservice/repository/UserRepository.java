package com.project.telecounselor.authservice.repository;

import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.telecounselor.model.entity.User;

/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the user module action in database.
 * In query annotation (nativeQuery = true) the below query perform like SQL.
 * Otherwise its perform like HQL default value for nativeQuery FALSE
 * </p>
 * 
 * @author VigneshKumar created on Aug 26, 2022
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long>, PagingAndSortingRepository<User, Long> {

	public static final String GET_USER_BY_USERNAME = "select user from User as user where user.username =:username and user.isActive =:status";

	/**
	 * <p>
	 * Used to get the user object by username.
	 * </p>
	 * 
	 * @return User
	 */
	@Query(value = GET_USER_BY_USERNAME)
	public User getUserByUsername(@Param("username") String username, @Param("status") Boolean status);

}
