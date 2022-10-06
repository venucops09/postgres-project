package com.mdtlabs.coreplatform.authservice.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.model.entity.Timezone;
import com.mdtlabs.coreplatform.common.model.entity.User;


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
	 * This method is used to get user data by passing username
	 * 
	 * @param username - user name of the user
	 * @param status   - active status of the user
	 * @return User - user information which is stored
	 */
	@Query(value = GET_USER_BY_USERNAME)
	public User getUserByUsername(@Param(Constants.USERNAME) String username, @Param(Constants.STATUS) Boolean status);

}
