package com.project.telecounselor.userservice.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.project.telecounselor.common.Constants;
import com.project.telecounselor.common.util.TokenParse;
import com.project.telecounselor.model.dto.UserDTO;
import com.project.telecounselor.model.dto.UserProfileDTO;
import com.project.telecounselor.model.entity.Country;
import com.project.telecounselor.model.entity.Timezone;
import com.project.telecounselor.model.entity.User;
import com.project.telecounselor.userservice.service.UserService;
import com.project.telecounselor.util.UserContextHolder;
import com.project.telecounselor.userservice.message.SuccessCode;
import com.project.telecounselor.userservice.message.SuccessResponse;

import io.swagger.annotations.Api;

/**
 * <p>
 * User Controller used to perform any action in the user module like read and
 * write.
 * </p>
 * 
 * @author VigneshKumar created on Jun 30, 2022
 */
@RestController
@RequestMapping(value = "/user")
@Api(basePath = "/user", value = "master_data", description = "User related APIs", produces = "application/json")
public class UserController {

	private static final String NO_DATA_FOUND = "No Data Found";

	private static final List<String> noDataList = Arrays.asList(NO_DATA_FOUND);
	@Autowired
	private UserService userService;

	@Value("${app.page-count}")
	private int gridDisplayValue;

	ModelMapper modelMapper = new ModelMapper();

	/**
	 * <p>
	 * Add new user with login credential, employee Id.
	 * </p>
	 * 
	 * @param user
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.POST)
	@TokenParse
	public SuccessResponse<UserDTO> addUser(@RequestBody User user){
		User addUser = userService.addUser(user);
		if (null != addUser) {
			UserDTO userDTO = modelMapper.map(addUser, UserDTO.class);
			return new SuccessResponse<UserDTO>(SuccessCode.USER_SAVE, userDTO, HttpStatus.OK);
		}
		return null;
	}

	/**
	 * Retrieve all the active users detail list.
	 * <p>
	 * </p>
	 * 
	 * @return List of User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/all/{pageNumber}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<List<User>> getAllUsers(@PathVariable(value = Constants.PAGE_NUMBER) int pageNumber) {
		List<User> allUsers = userService.getAllUsersWithInOrganization(pageNumber);
		if (allUsers.isEmpty()) {
			return new SuccessResponse(SuccessCode.GET_USERS, noDataList, HttpStatus.OK);
		}
		List<UserDTO> users = modelMapper.map(allUsers, new TypeToken<List<UserDTO>>() {
		}.getType());
		int count = userService.getTenantTotalSize();
		int totalPageNumber = count / gridDisplayValue;
		int remainder = count % gridDisplayValue;
		totalPageNumber = (remainder == 0) ? (totalPageNumber + 0) : (totalPageNumber + 1);
		return new SuccessResponse(SuccessCode.GET_USERS, users, totalPageNumber, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Update User details like password, name etc.
	 * </p>
	 * 
	 * @param user
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(method = RequestMethod.PUT)
	@TokenParse
	public SuccessResponse<User> updateUser(@RequestBody User user) {
		return new SuccessResponse(SuccessCode.USER_UPDATE, userService.updateUser(user), HttpStatus.OK);
	}

	/**
	 * <p>
	 * Deactivate the user account using their id.
	 * </p>
	 * 
	 * @param userId
	 * @return Success Count
	 * @throws Exception
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	@TokenParse
	public SuccessResponse<Boolean> deleteUserById(@PathVariable(value = Constants.ID) long userId) {
		return new SuccessResponse(SuccessCode.USER_DELETE, userService.deleteUserById(userId), HttpStatus.OK);
	}

	/**
	 * <p>
	 * Retrieve User detail using id.
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<User> getUserById(@PathVariable(value = Constants.ID) long userId) {
		User user = userService.getUserById(userId);
		return new SuccessResponse(SuccessCode.GET_USER, user, HttpStatus.OK);
	}
	
	/**
	 * <p>
	 * Retrieve User detail using id.
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/get-user/{id}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<User> getAllUserById(@PathVariable(value = Constants.ID) long userId) {
		return new SuccessResponse(SuccessCode.GET_USER, userService.getAllUserById(userId), HttpStatus.OK);
	}
	
	/**
	 * <p>
	 * Used to get user profile using token.
	 * </p>
	 * 
	 * @throws Exception
	 */
	@RequestMapping(value = "/get-profile", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<UserProfileDTO> getUserProfileById() {
		UserDTO userDTO = UserContextHolder.getUserDto();
		User user = userService.getUserById(userDTO.getId());
		if (null != user) {
			UserProfileDTO userProfileDTO = modelMapper.map(user, UserProfileDTO.class);
			return new SuccessResponse<UserProfileDTO>(SuccessCode.GET_USER, userProfileDTO, HttpStatus.OK);
		}
		return new SuccessResponse<UserProfileDTO>(SuccessCode.FETCH_ERROR, user, HttpStatus.OK);
	}
	

	/**
	 * <p>
	 * Retrieve User detail using user name. For spring security auth
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/user-id/{username}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<User> getUserByUsername(@PathVariable(value = Constants.USERNAME) String username) {
		return new SuccessResponse<User>(SuccessCode.GET_USER, userService.getUserByUsername(username), HttpStatus.OK);
	}

	/**
	 * <p>
	 * Retrieve User Role Permission details of the logged in user by user id.
	 * </p>
	 * 
	 * @param userRolePermissionId
	 * @return User Role Permission Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/meta", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<Map<String, List<Map<String, Object>>>> getUserMeta() {
		return new SuccessResponse<Map<String, List<Map<String, Object>>>>(SuccessCode.GET_USER_META_DATA,
				userService.getUserMeta(), HttpStatus.OK);
	}

	/**
	 * This controller method is used to update the password of current user based
	 * on given emailId and new password.
	 * 
	 * @param token
	 * @param password
	 * @return SuccessResponse
	 */
	@RequestMapping(value = "/update-password/{token}", method = RequestMethod.PUT)
	public SuccessResponse<Boolean> saveOrUpdateUserName(@PathVariable("token") String token,
			@RequestBody Map<String, String> userInfo) {
		return new SuccessResponse<Boolean>(SuccessCode.PASSWORD_UPDATED, userService.updateUserPassword(token, userInfo.get(Constants.PASSWORD)),
				HttpStatus.OK);
	}

	/**
	 * This method is used to send password reset link email to the given username.
	 * 
	 * @param email
	 * @return SuccessResponse
	 */
	@RequestMapping(value = "/forgot-password/{email}", method = RequestMethod.GET, produces = "application/json")
	public SuccessResponse<Boolean> resetOrUpdatePassWord(@PathVariable("email") String email) {
		return new SuccessResponse<Boolean>(SuccessCode.SEND_EMAIL_USING_SMTP,
				userService.resetOrUpdatePassWord(email, false), HttpStatus.OK);
	}

	/**
	 * This method is used to send password reset link email to the given username.
	 * 
	 * @param email
	 * @return SuccessResponse
	 */
	@RequestMapping(value = "/change-password", method = RequestMethod.PUT, produces = "application/json")
	@TokenParse
	public SuccessResponse<Boolean> changePassword(@RequestBody Map<String, String> userInfo) {
		Boolean response = userService.changeOrUpdatePassword(userInfo);
		if (response) {
			return new SuccessResponse<Boolean>(SuccessCode.PASSWORD_UPDATED, response, HttpStatus.OK);
		}
		return new SuccessResponse<Boolean>(SuccessCode.PASSWORD_DOES_NOT_MATCH, response, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Retrieve User detail using user name. For spring security auth
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/name/{name}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<List<User>> getUserByFirstName(@PathVariable(value = Constants.NAME) String name) {
		return new SuccessResponse(SuccessCode.GET_USER, userService.getUserByFirstName(name), HttpStatus.OK);
	}

	/**
	 * <p>
	 * Retrieve User detail using user name. For spring security auth
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/user-token/{id}", method = RequestMethod.PUT)
	@TokenParse
	public SuccessResponse<User> updateUserToken(@PathVariable(Constants.ID) long id,
			@RequestBody Map<String, String> userInfo) {
		return new SuccessResponse(SuccessCode.GET_USER, userService.updateUserToken(id, userInfo), HttpStatus.OK);
	}

	/**
	 * <p>
	 * Retrieve User details using role
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/role-name/{id}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<List<User>> getUserByRoleName(@PathVariable(Constants.ID) String id) {
		return new SuccessResponse(SuccessCode.GET_USER, userService.getUserByRoleName(Long.valueOf(id)),
				HttpStatus.OK);
	}

	@RequestMapping(value = "/tenant/{id}", method = RequestMethod.DELETE)
	@TokenParse
	public SuccessResponse<List<User>> deletedAllUserInTenant(@PathVariable(Constants.ID) long tenantId) {
		boolean isUserDeleted = userService.deletedAllUserInTenant(tenantId);
		return isUserDeleted ? new SuccessResponse(SuccessCode.USER_DELETE, "Success", HttpStatus.OK)
				: new SuccessResponse(SuccessCode.USER_DELETE, NO_DATA_FOUND, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<List<UserDTO>> getAllUserInTenant() {
		List<User> allUsers = userService.getAllUserInTenant();
		List<UserDTO> users = modelMapper.map(allUsers, new TypeToken<List<UserDTO>>() {
		}.getType());
		return !users.isEmpty() ? new SuccessResponse(SuccessCode.GET_USERS, users, users.size(), HttpStatus.OK)
				: new SuccessResponse(SuccessCode.GET_USERS, NO_DATA_FOUND, 0, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Return false if user exceed the invalid login limit.
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/isLoginLimitExceed/{username}", method = RequestMethod.GET)
	public SuccessResponse<Boolean> isLoginLimitExceed(@PathVariable(value = Constants.USERNAME) String username) {
		Boolean response = userService.isLoginLimitExceed(username);
		return response ? new SuccessResponse<Boolean>(SuccessCode.DISABLED_ACCOUNT, response, HttpStatus.OK)
				: new SuccessResponse<Boolean>(SuccessCode.PASSWORD_DOES_NOT_MATCH, response, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Return false if user exceed the forget passowrd limit.
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/isForgetPasswordLimitExceed/{username}", method = RequestMethod.POST)
	public SuccessResponse<Boolean> isForgetPasswordLimitExceed(
			@PathVariable(value = Constants.USERNAME) String username) {
		Boolean response = userService.isForgetPasswordLimitExceed(username);
		return response ? new SuccessResponse<Boolean>(SuccessCode.DISABLED_ACCOUNT, response, HttpStatus.OK)
				: new SuccessResponse<Boolean>(SuccessCode.SEND_EMAIL_USING_SMTP, response, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Return false if user exceed the forget passowrd limit.
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/isResetPasswordLimitExceed/{username}", method = RequestMethod.POST)
	public SuccessResponse<Boolean> isResetPasswordLimitExceed(
			@PathVariable(value = Constants.USERNAME) String username) {
		Boolean response = userService.isResetPasswordLimitExceed(username);
		return response ? new SuccessResponse<Boolean>(SuccessCode.DISABLED_ACCOUNT, response, HttpStatus.OK)
				: new SuccessResponse<Boolean>(SuccessCode.PASSWORD_UPDATED, response, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Retrieve all the active users detail list.
	 * </p>
	 * 
	 * @return List of User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "users/{roleName}/{pageNumber}/{limit}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<List<User>> getUsersByRolename(@PathVariable(value = Constants.ROLE_NAME) String roleName,
			@PathVariable(value = Constants.PAGE_NUMBER) String pageNumber,
			@PathVariable(value = Constants.LIMIT) String limit) {
		List<User> allUsers = userService.getUsersByRolename(roleName, Integer.valueOf(pageNumber),
				Integer.valueOf(limit));
		if (allUsers.isEmpty()) {
			return new SuccessResponse(SuccessCode.GET_USERS, noDataList, HttpStatus.OK);
		}
		List<UserDTO> users = modelMapper.map(allUsers, new TypeToken<List<UserDTO>>() {
		}.getType());
		int count = userService.getTenantTotalSize();
		int totalPageNumber = count / gridDisplayValue;
		int remainder = count % gridDisplayValue;
		totalPageNumber = (remainder == 0) ? (totalPageNumber + 0) : (totalPageNumber + 1);
		return new SuccessResponse(SuccessCode.GET_USERS, users, totalPageNumber, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Get the country list.
	 * </p>
	 * 
	 * @return country list
	 * @throws Exception
	 */
	@RequestMapping(value = "/country", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<List<Country>> getCountryList() {
		return new SuccessResponse<List<Country>>(SuccessCode.GET_ALL_COUNTRY, userService.getCountryList(),
				HttpStatus.OK);
	}

	/**
	 * <p>
	 * Get the timezone list.
	 * </p>
	 * 
	 * @return timezone list
	 * @throws Exception
	 */
	@RequestMapping(value = "/timezone", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<List<Timezone>> getTimezoneList() {
		return new SuccessResponse<List<Timezone>>(SuccessCode.GET_ALL_TIMEZONE, userService.getTimezoneList(),
				HttpStatus.OK);
	}
	
	/**
	 * <p>
	 * Retrieve User detail using user name. For spring security auth
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/validate-user/{username}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<User> getAllUserByUsername(@PathVariable(value = Constants.USERNAME) String username) {
		User user = userService.getAllUserByUsername(username);
		return new SuccessResponse<User>(SuccessCode.GET_USER, user, HttpStatus.OK);
	}
	
	/**
	 * <p>
	 * Retrieve User detail using user name. For spring security auth
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/get-country/{id}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<Country> getCountryById(@PathVariable(value = Constants.ID) String id) {
		return new SuccessResponse<Country>(SuccessCode.GET_COUNTRY, userService.getCountryById(id), HttpStatus.OK);
	}
	
	/**
	 * <p>
	 * Retrieve User detail using user name. For spring security auth
	 * </p>
	 * 
	 * @return User Entity
	 * @throws Exception
	 */
	@RequestMapping(value = "/get-timezone/{id}", method = RequestMethod.GET)
	@TokenParse
	public SuccessResponse<Timezone> getTimezoneById(@PathVariable(value = Constants.ID) String id) {
		return new SuccessResponse<Timezone>(SuccessCode.GET_TIMEZONE, userService.getTimezoneById(id), HttpStatus.OK);
	}
	
}