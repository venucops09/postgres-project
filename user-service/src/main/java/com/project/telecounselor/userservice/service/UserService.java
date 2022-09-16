package com.project.telecounselor.userservice.service;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import com.project.telecounselor.model.entity.Country;
import com.project.telecounselor.model.entity.Timezone;
import com.project.telecounselor.model.entity.User;

/**
 * <p>
 * This an interface class for user module you can implemented this class in any
 * class.
 * </p>
 * 
 * @author VigneshKumar created on Jun 30, 2022
 */
public interface UserService {

	/**
	 * <p>
	 * This method used to add a new user account.
	 * </p>
	 * 
	 * @param user
	 * @return User Entity
	 */
	User addUser(User user);

	/**
	 * <p>
	 * This method used to retrieve all the active user account.
	 * </p>
	 * 
	 * @param pageNumber
	 * @return List of User Entity
	 */
	List<User> getAllUsers(int pageNumber);

	/**
	 * <p>
	 * This method used to update a user account details.
	 * </p>
	 * 
	 * @param user
	 * @return User Entity
	 */
	User updateUser(User user);

	/**
	 * <p>
	 * This method used to inactive a user account using id.
	 * </p>
	 * 
	 * @param userId
	 * @return Success count.
	 */
	boolean deleteUserById(long userId);

	/**
	 * <p>
	 * This method used to get a user account detail by id.
	 * </p>
	 * 
	 * @param userId
	 * @param status
	 * @return User Entity
	 */
	User getUserById(long userId);

	/**
	 * <p>
	 * This method will retrieve user based on user name. Used for spring security
	 * authentication.
	 * </p>
	 * 
	 * @param username
	 * @return
	 */
	User getUserByUsername(String username);
	
	/**
	 * <p>
	 * This method will retrieve user based on user name.
	 * authentication.
	 * </p>
	 * 
	 * @param username
	 * @return
	 */
	User getAllUserByUsername(String username);
	
	/**
	 * <p>
	 * This method will retrieve user based on id.
	 * authentication.
	 * </p>
	 * 
	 * @param username
	 * @return
	 */
	User getAllUserById(long id);
	

	/**
	 * <p>
	 * This method is used to update user with new password
	 * </p>
	 * 
	 * @param token
	 * @param password
	 * @return Boolean
	 */
	Boolean updateUserPassword(String token, String password);

	/**
	 * <p>
	 * This method is used to sent reset password link to the specific user.
	 * </p>
	 * 
	 * @param id
	 * @param password
	 * @return Boolean
	 */
	Boolean resetOrUpdatePassWord(String email, boolean isFromCreation);

	/**
	 * This method change is used to change the user old password with new password.
	 * 
	 * @param userInfo
	 * @return
	 */
	Boolean changeOrUpdatePassword(Map<String, String> userInfo);

	/**
	 * <p>
	 * This method is used for obtaining the list of users based on user name
	 * </p>
	 * 
	 * @param username
	 * @return List of User
	 */
	List<User> getUserByFirstName(String name);

	/**
	 * <p>
	 * This method is used for updating the user token in the table
	 * </p>
	 * 
	 * @return user
	 */
	int updateUserToken(long id, Map<String, String> userInfo);

	/**
	 * <p>
	 * Get user details by role id with in organization.
	 * </p>
	 * 
	 * @param id
	 * @param jwtToken
	 * @return List of user
	 */
	List<User> getUserByRoleName(long id);

	/**
	 * <p>
	 * Delete all users in particular tenant.
	 * </p>
	 * 
	 * @param tenantId
	 * @return Boolean
	 */
	boolean deletedAllUserInTenant(long tenantId);

	/**
	 * <p>
	 * Get total soze of tenant.
	 * </p>
	 * 
	 * @return int
	 */
	int getTenantTotalSize();

	/**
	 * <p>
	 * Get user meta data.
	 * </p>
	 * 
	 * @return <String, List<Map<String, Object>>>
	 */
	Map<String, List<Map<String, Object>>> getUserMeta();

	/**
	 * <p>
	 * Get all users within specific organization.
	 * </p>
	 * 
	 * @param pageNumber
	 * @return List<User>
	 */
	List<User> getAllUsersWithInOrganization(int pageNumber);

	/**
	 * <p>
	 * Get all users in tenant.
	 * </p>
	 * 
	 * @return List<User>
	 */
	List<User> getAllUserInTenant();

	/**
	 * <p>
	 * To check login limit exceeds or not.
	 * </p>
	 * 
	 * @param username
	 * @return Boolean
	 */
	boolean isLoginLimitExceed(String username);

	/**
	 * <p>
	 * To check forget password limit exceeds or not.
	 * </p>
	 * 
	 * @param username
	 * @return Boolean
	 */
	boolean isForgetPasswordLimitExceed(String username);

	/**
	 * <p>
	 * To check reset password limit exceeds or not.
	 * </p>
	 * 
	 * @param username
	 * @return Boolean
	 */
	boolean isResetPasswordLimitExceed(String username);

	/**
	 * <p>
	 * To get users based on role.
	 * </p>
	 * 
	 * @param roleName
	 * @param pageNumber
	 * @param limit
	 * @return List<User>
	 */
	List<User> getUsersByRolename(String roleName, int pageNumber, int limit);

	/**
	 * <p>
	 * To get users based on username but not by given id.
	 * </p>
	 * 
	 * @param username
	 * @param userId
	 * @return User
	 */
	User getUserByUsernameNotById(String username, long userId);

	/**
	 * <p>
	 * This method used to get all country list.
	 * </p>
	 * 
	 * @return List
	 */
	List<Country> getCountryList();

	/**
	 * <p>
	 * This method used to get all timezone list.
	 * </p>
	 * 
	 * @return List
	 */
	List<Timezone> getTimezoneList();
	
	/**
	 * <p>
	 * This method used to get all timezone bt id.
	 * </p>
	 * 
	 * @return List
	 */
	Timezone getTimezoneById(String id);
	
	/**
	 * <p>
	 * This method used to get all country bt id.
	 * </p>
	 * 
	 * @return List
	 */
	Country getCountryById(String id);
}
