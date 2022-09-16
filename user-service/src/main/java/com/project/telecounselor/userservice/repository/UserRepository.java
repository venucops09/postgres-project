package com.project.telecounselor.userservice.repository;

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

import com.project.telecounselor.model.entity.Country;
import com.project.telecounselor.model.entity.Timezone;
import com.project.telecounselor.model.entity.User;

/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the user module action in database.
 * In query annotation (nativeQuery = true) the below query perform like SQL.
 * Otherwise its perform like HQL default value for nativeQuery FALSE
 * </p>
 * 
 * @author VigneshKumar created on Jun 20, 2022
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long>, PagingAndSortingRepository<User, Long> {

	public static final String GET_ALL_USERS = "select user from User as user where user.isActive =:status";
	public static final String GET_ALL_ACTIVE_USERS = "select user from User as user where user.isActive =:status";
	public static final String GET_ALL_USERS_BY_ROLE = "select user from User as user join user.roles role where user.isActive =:status and role.name =:roleName order by user.updatedAt desc";
	public static final String GET_ALL_USERS_IN_ORGANIZATION_ROLE = "select user from User as user join user.roles role where user.isActive =:status and user.tenantId =:tenantId and role.id IN (:roleIds)";
	public static final String GET_USER_BY_ID = "select user from User as user where user.id =:userId and user.isActive =:status";
	public static final String GET_USER_BY_USERNAME = "select user from User as user where user.username =:username and user.isActive =:status";
	public static final String GET_VALID_USER_BY_ID = "select user from User as user where user.id =:id";
	public static final String GET_VALID_USER_BY_USERNAME = "select user from User as user where user.username =:username";
	public static final String DELETE_USER_BY_ID = "delete from User as user  where user.id =:userId";
	public static final String UPDATE_USER_STATUS_BY_ID = "update User as user set user.isActive =:status where user.id =:userId";
	public static final String GET_USER_BY_NAME = "Select user from User as user join user.roles role where user.isActive = true and user.tenantId =:tenantId and UPPER(user.firstName) LIKE ('%' || UPPER(:name) || '%') and role.id IN (:roleIds)";
	public static final String UPDATE_USER_TOKEN = "update User as user set user.authToken =:jwtToken, user.refreshToken =:jwtRefreshToken where user.id =:id";
	public static final String GET_ALL_ACTIVE_USERS_BY_ROLE = "select user from User as user join user.roles as role where role.id =:roleId and user.tenantId =:tenantId and user.isActive = true";
	public static final String DELETE_ALL_TENANT_BY_TENANT_ID = "update User as user set user.isActive = false where user.tenantId =:tenantId";
	public static final String GET_ALL_USERS_IN_ORGANIZATION = "select user from User as user where user.isActive = true and user.tenantId =:tenantId";
	public static final String GET_USERS_BY_USERNAME_NOT_BY_ID = "select user from User as user where user.isActive =:status and user.username =:username and user.id !=:userId";
	public static final String GET_ALL_COUNTRY = "select country from Country as country where country.isActive = true";
	public static final String GET_ALL_TIMEZONE = "select timezone from Timezone as timezone where timezone.isActive = true";
	public static final String GET_COUNTRY_BY_ID = "select country from Country as country where country.id =:id and country.isActive = true";
	public static final String GET_TIMEZONE_BY_ID = "select timezone from Timezone as timezone where timezone.id =:id and timezone.isActive = true";
	
	/**
	 * <p>
	 * This method get all the active user details from the database.
	 * </p>
	 * 
	 * @param status
	 * @return List of User Entity
	 */
	@Query(value = GET_ALL_USERS)
	Page<User> getAllUser(Pageable pageable);

	/**
	 * <p>
	 * This method get all the active user details from the database.
	 * </p>
	 * 
	 * @param status
	 * @return List of User Entity
	 */
	@Query(value = GET_ALL_ACTIVE_USERS)
	public List<User> getAllUsers(@Param("status") Boolean status);

	/**
	 * <p>
	 * This method used to get the user account detail using id.
	 * </p>
	 * 
	 * @param userId
	 * @return User Entity
	 */
	@Query(value = GET_USER_BY_ID)
	public User getUserById(@Param("userId") long userId, @Param("status") Boolean status);

	/**
	 * <p>
	 * Used to get the user object by username.
	 * </p>
	 * 
	 * @return User
	 */
	@Query(value = GET_USER_BY_USERNAME)
	public User getUserByUsername(@Param("username") String username, @Param("status") Boolean status);


	/**
	 * <p>
	 * Used to get the user object by username.
	 * </p>
	 * 
	 * @return User
	 */
	@Query(value = GET_VALID_USER_BY_USERNAME)
	public User getAllUserByUsername(@Param("username") String username);
	
	/**
	 * <p>
	 * Used to get the user object by id.
	 * </p>
	 * 
	 * @return User
	 */
	@Query(value = GET_VALID_USER_BY_ID)
	public User getAllUserById(@Param("id") long id);
	
	/**
	 * <p>
	 * delete user by userId.
	 * </p>
	 * 
	 * @param userId
	 * @return status count
	 */
	@Modifying
	@Transactional
	@Query(value = DELETE_USER_BY_ID)
	public int deleteUserbyId(@Param("userId") Long userId);

	/**
	 * <p>
	 * This method used to active or inactive the user account using id (0 as
	 * inactive and 1 as active).
	 * </p>
	 * 
	 * @param status
	 * @param userId
	 * @return User Entity
	 */
	@Modifying
	@Transactional
	@Query(value = UPDATE_USER_STATUS_BY_ID)
	public int updateUserStatusById(@Param("status") Boolean status, @Param("userId") long userId);

	/**
	 * <p>
	 * This method is used for obtaining the list of user
	 * </p>
	 * 
	 * @return List of user
	 */
	@Query(value = GET_USER_BY_NAME)
	public List<User> getUserByFirstName(@Param("name") String name, @Param("tenantId") long tenantId,
			@Param("roleIds") Set<Long> roleIds);

	/**
	 * <p>
	 * This method is used for updating the token of user
	 * </p>
	 * 
	 * @return updated user
	 */
	@Modifying
	@Transactional
	@Query(value = UPDATE_USER_TOKEN)
	int updateUserToken(long id, String jwtToken, String jwtRefreshToken);

	/**
	 * <p>
	 * Get user details by role id with in organization.
	 * </p>
	 * 
	 * @param roleId
	 * @param tenantId
	 * @return List of User
	 */
	@Query(value = GET_ALL_ACTIVE_USERS_BY_ROLE)
	List<User> getUserByRoleName(@Param("roleId") long id, @Param("tenantId") long tenantId);

	/**
	 * <p>
	 * Get user details by username but not by gicen id.
	 * </p>
	 * 
	 * @param username
	 * @param userId
	 * @return List of User
	 */
	@Query(value = GET_USERS_BY_USERNAME_NOT_BY_ID)
	User getUserByUsernameNotById(@Param("username") String username, @Param("userId") long userId,
			@Param("status") Boolean status);

	/**
	 * <p>
	 * Get all the users with in tenant.
	 * </p>
	 * 
	 * @param pageable
	 * @return List of User.
	 */
	@Query(value = GET_ALL_USERS_IN_ORGANIZATION_ROLE)
	Page<User> getAllUsersWithInOrganization(@Param("status") Boolean status, @Param("tenantId") long tenantId,
			@Param("roleIds") Set<Long> roleIds, Pageable pageable);

	/**
	 * <p>
	 * Get all the users with in tenant.
	 * </p>
	 * 
	 * @return List of User.
	 */
	@Query(value = GET_ALL_USERS_IN_ORGANIZATION_ROLE)
	List<User> getAllUsersWithInOrganization(@Param("status") Boolean status, @Param("tenantId") long tenantId,
			@Param("roleIds") Set<Long> roleIds);

	/**
	 * <p>
	 * Inactive all the user from the given organization.
	 * </p>
	 * 
	 * @param tenantId
	 */
	@Query(value = DELETE_ALL_TENANT_BY_TENANT_ID)
	@Transactional
	@Modifying
	void deletedAllUserInTenant(@Param("tenantId") long tenantId);

	/**
	 * <p>
	 * Get all the users with in tenant.
	 * </p>
	 * 
	 * @return List of User.
	 */
	@Query(value = GET_ALL_USERS_IN_ORGANIZATION)
	List<User> getAllUserByTenantId(@Param("tenantId") long tenantId);

	/**
	 * <p>
	 * Get all the users based on role
	 * </p>
	 * 
	 * @param status
	 * @param pageable
	 * @param roleName
	 * @return List of User.
	 */
	@Query(value = GET_ALL_USERS_BY_ROLE)
	Page<User> getUsersByRolename(@Param("status") Boolean status, @Param("roleName") String roleName,
			Pageable pageable);

	/**
	 * <p>
	 * Get all the country list.
	 * </p>
	 * 
	 * @return List of country.
	 */
	@Query(value = GET_ALL_COUNTRY)
	List<Country> getCountryList();

	/**
	 * <p>
	 * Get all the timezone list.
	 * </p>
	 * 
	 * @return List of timezone.
	 */
	@Query(value = GET_ALL_TIMEZONE)
	List<Timezone> getTimezoneList();
	
	/**
	 * <p>
	 * Used to get the country object by id.
	 * </p>
	 * 
	 * @return User
	 */
	@Query(value = GET_COUNTRY_BY_ID)
	public Country getCountryById(@Param("id") String id);
	
	/**
	 * <p>
	 * Used to get the country object by id.
	 * </p>
	 * 
	 * @return User
	 */
	@Query(value = GET_TIMEZONE_BY_ID)
	public Timezone getTimezoneById(@Param("id") String id);
	
}
