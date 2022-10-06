package com.mdtlabs.coreplatform.userservice.service;

import java.util.List;
import java.util.Map;

import com.mdtlabs.coreplatform.common.model.entity.User;
import com.mdtlabs.coreplatform.common.model.entity.UserToken;


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
	 * @param user - user to be added
	 * @return User - user entity
	 */
	User addUser(User user);

	/**
	 * <p>
	 * This method used to update a user account details.
	 * </p>
	 * 
	 * @param user - user to be updated
	 * @return User - user entity
	 */
	User updateUser(User user);

	/**
	 * <p>
	 * This method used to inactive a user account using id.
	 * </p>
	 * 
	 * @param userId - user id to be deleted
	 * @return boolean - state of deleted user as true or false
	 */
	boolean deleteUserById(long userId);

	/**
	 * This method is used to get user with respect to id
	 * 
	 * @param userId - user id
	 * @return User - user entity
	 */
	User getUserById(long userId);

	/**
	 * <p>
	 * This method will retrieve user based on user name. Used for spring security
	 * authentication.
	 * </p>
	 * 
	 * @param username - user name of user
	 * @return User - user entity
	 */
	User getUserByUsername(String username);

	/**
	 * <p>
	 * This method is used to update user with new password
	 * </p>
	 * 
	 * @param token    - authorization token of logged in user
	 * @param password - password to be updated
	 * @return Boolean - status of update as true or false
	 */
	Boolean updatePassword(String token, String password);

	/**
	 * This method is used to reset or update password
	 * 
	 * @param email          - email of the user
	 * @param isFromCreation - form availablity as true or false
	 * @return boolean - status of password reset or update as true or false
	 */
	Boolean forgotPassword(String email, boolean isFromCreation);

	/**
	 * This method is used to update user token
	 * 
	 * @param id       - user id
	 * @param userInfo - token information in map
	 * @return int - response of token update
	 */
	UserToken createUserToken(long id, Map<String, String> userInfo);

	/**
	 * <p>
	 * Get total soze of tenant.
	 * </p>
	 * 
	 * @return int - count of total tenant
	 */
	int getTotalSize();

	/**
	 * <p>
	 * Get all users within specific organization.
	 * </p>
	 * 
	 * @param pageNumber - page number of the table
	 * @return List<User> - list of user entity
	 */
	List<User> getUsers(int pageNumber);

	/**
	 * <p>
	 * To check login limit exceeds or not.
	 * </p>
	 * 
	 * @param username - user name of user
	 * @return Boolean - true or false on login limit exceed state
	 */
	boolean loginLimitExceed(String username);

	/**
	 * <p>
	 * To check forget password limit exceeds or not.
	 * </p>
	 * 
	 * @param username - user name of user
	 * @return Boolean - true or false of forget password limit exceed state
	 */
	boolean forgetPasswordLimitExceed(String username);

	/**
	 * <p>
	 * To check reset password limit exceeds or not.
	 * </p>
	 * 
	 * @param username - user name of user
	 * @return Boolean - true or false of reset password limit exceed state
	 */
	boolean resetPasswordLimitExceed(String username);
	
	/**
	 * This method is used to clear the api role permission map
	 * 
	 */
	void clearApiPermissions();

}
