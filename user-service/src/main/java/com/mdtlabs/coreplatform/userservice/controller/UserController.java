package com.mdtlabs.coreplatform.userservice.controller;

import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdtlabs.coreplatform.userservice.message.SuccessCode;
import com.mdtlabs.coreplatform.userservice.message.SuccessResponse;
import com.mdtlabs.coreplatform.userservice.service.UserService;
import com.mdtlabs.coreplatform.common.Constants;
import com.mdtlabs.coreplatform.common.FieldConstants;
import com.mdtlabs.coreplatform.common.contexts.UserContextHolder;
import com.mdtlabs.coreplatform.common.model.dto.UserDTO;
import com.mdtlabs.coreplatform.common.model.dto.UserProfileDTO;
import com.mdtlabs.coreplatform.common.model.entity.User;
import com.mdtlabs.coreplatform.common.model.entity.UserToken;
import com.mdtlabs.coreplatform.common.util.CommonUtil;

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
@Api(value = Constants.MASTER_DATA, produces = Constants.APPLICATION_JSON)
public class UserController {

	@Autowired
	private UserService userService;

	@Value("${app.page-count}")
	private int gridDisplayValue;

	ModelMapper modelMapper = new ModelMapper();

	/**
	 * This method is used to add user information
	 * 
	 * @param user - user to be added
	 * @return UserDTO - response of adding new user
	 */
	@PostMapping
	public SuccessResponse<UserDTO> addUser(@RequestBody User user) {
		User addUser = userService.addUser(user);
		if (null != addUser) {
			UserDTO userDTO = modelMapper.map(addUser, UserDTO.class);
			return new SuccessResponse<>(SuccessCode.USER_SAVE, userDTO, HttpStatus.OK);
		}
		return new SuccessResponse<>(SuccessCode.USER_NOT_SAVED, user, HttpStatus.BAD_REQUEST);
	}

	/**
	 * This method is used to get all users information
	 * 
	 * @param pageNumber - page number of the grid
	 * @return List<User> - list response of the users
	 */
	@GetMapping(value = "/all/{pageNumber}")
	public SuccessResponse<List<UserDTO>> getUsers(@PathVariable(value = Constants.PAGE_NUMBER) int pageNumber) {
		List<User> allUsers = userService.getUsers(pageNumber);
		if (allUsers.isEmpty()) {
			return new SuccessResponse(SuccessCode.GET_USERS, Constants.NO_DATA_LIST, HttpStatus.OK);
		}
		List<UserDTO> users = modelMapper.map(allUsers, new TypeToken<List<UserDTO>>() {
		}.getType());
		return new SuccessResponse(SuccessCode.GET_USERS, users, userService.getTotalSize(), HttpStatus.OK);
	}

	/**
	 * <p>
	 * Update User details like password, name etc.
	 * </p>
	 * 
	 * @param user - user to be updated
	 * @return User - response of the updated user
	 */
	@PutMapping
	public SuccessResponse<UserDTO> updateUser(@RequestBody User user) {
		UserDTO userDto = modelMapper.map(userService.updateUser(user), UserDTO.class);
		return new SuccessResponse(SuccessCode.USER_UPDATE, userDto, HttpStatus.OK);
	}

	/**
	 * <p>
	 * Deactivate the user account using their id.
	 * </p>
	 * 
	 * @param userId - id of the user
	 * @return boolean - count response of the deleted user
	 */
	@PutMapping(value= "/delete/{id}")
	public SuccessResponse<Boolean> deleteUserById(@PathVariable(value = FieldConstants.ID) long userId) {
		return new SuccessResponse(SuccessCode.USER_DELETE, userService.deleteUserById(userId), HttpStatus.OK);
	}

	/**
	 * This method is used to get user by id
	 * 
	 * @param userId - id of the user
	 * @return User - response of the user
	 */
	@GetMapping(value = "/{id}")
	public SuccessResponse<UserDTO> getUserById(@PathVariable(value = FieldConstants.ID) long userId) {
		UserDTO userDTO = modelMapper.map(userService.getUserById(userId), UserDTO.class);
		return new SuccessResponse(SuccessCode.GET_USER, userDTO, HttpStatus.OK);
	}

	/**
	 * This method is used to get user profile by id
	 * 
	 * @return UserProfileDTO - response of user profile
	 */
	@GetMapping(value = "/profile")
	public SuccessResponse<UserProfileDTO> getUserProfileById() {
		UserDTO userDTO = UserContextHolder.getUserDto();
		User user = userService.getUserById(userDTO.getId());
		if (null != user) {
			UserProfileDTO userProfileDTO = modelMapper.map(user, UserProfileDTO.class);
			return new SuccessResponse<>(SuccessCode.GET_USER, userProfileDTO, HttpStatus.OK);
		}
		return new SuccessResponse<>(SuccessCode.USER_NOT_FOUND, user, HttpStatus.OK);
	}

	/**
	 * This method is used to get user by user name
	 * 
	 * @param username - username of the user
	 * @return User - response of the user
	 */
	@GetMapping(value = "username/{username}")
	public SuccessResponse<User> getUserByUsername(@PathVariable(value = FieldConstants.USERNAME) String username) {
		return new SuccessResponse<>(SuccessCode.GET_USER, userService.getUserByUsername(username), HttpStatus.OK);
	}

	/**
	 * This method is used to save or update user name of user
	 * 
	 * @param token    - auth token of logged in user
	 * @param userInfo - password information of the user
	 * @return boolean - response of updating user as true/false
	 */
	@PutMapping(value = "/update-password/{token}")
	public SuccessResponse<Boolean> updatePassword(@PathVariable(Constants.TOKEN) String token,
			@RequestBody Map<String, String> userInfo) {
		return new SuccessResponse<>(SuccessCode.PASSWORD_UPDATED,
				userService.updatePassword(token, userInfo.get(FieldConstants.PASSWORD)), HttpStatus.OK);
	}

	/**
	 * This method is used to send password reset link email to the given username.
	 * 
	 * @param email - email of the users
	 * @return boolean - response as true or false on reset
	 */
	@GetMapping(value = "/forgot-password/{email}", produces = Constants.APPLICATION_JSON)
	public SuccessResponse<Boolean> forgotPassword(@PathVariable(Constants.EMAIL) String email) {
		return new SuccessResponse<>(SuccessCode.SEND_EMAIL_USING_SMTP,
				userService.forgotPassword(email, Boolean.FALSE), HttpStatus.OK);
	}

	/**
	 * This method is used to update user token
	 * 
	 * @param id       - id of the user
	 * @param userInfo - user information as map
	 * @return User - response of the user token update
	 */
	@PostMapping(value = "/user-token/{id}")
	public SuccessResponse<UserToken> createUserToken(@PathVariable(FieldConstants.ID) long id,
			@RequestBody Map<String, String> userInfo) {
		UserToken userToken = modelMapper.map(userService.createUserToken(id, userInfo), UserToken.class);
		return new SuccessResponse(SuccessCode.GET_USER, userToken, HttpStatus.OK);
	}

	/**
	 * This method is used to check login limit exceeded or not
	 * 
	 * @param username - user name of the user
	 * @return boolean - response of the limit exceed as true or false
	 */
	@GetMapping(value = "/login-limit-exceed/{username}")
	public SuccessResponse<Boolean> loginLimitExceed(@PathVariable(value = FieldConstants.USERNAME) String username) {
		Boolean response = userService.loginLimitExceed(username);
		return Boolean.TRUE.equals(response)
				? new SuccessResponse<>(SuccessCode.DISABLED_ACCOUNT, response, HttpStatus.OK)
				: new SuccessResponse<>(SuccessCode.PASSWORD_DOES_NOT_MATCH, response, HttpStatus.OK);
	}

	/**
	 * This method is used to check forget password limit exceeded or not
	 * 
	 * @param username - user name fo the user
	 * @return Boolean - reponse of limit exceeded as true or false
	 */
	@PostMapping(value = "/forget-password-limit-exceed/{username}")
	public SuccessResponse<Boolean> forgetPasswordLimitExceed(
			@PathVariable(value = FieldConstants.USERNAME) String username) {
		Boolean response = userService.forgetPasswordLimitExceed(username);
		return Boolean.TRUE.equals(response)
				? new SuccessResponse<>(SuccessCode.DISABLED_ACCOUNT, response, HttpStatus.OK)
				: new SuccessResponse<>(SuccessCode.SEND_EMAIL_USING_SMTP, response, HttpStatus.OK);
	}

	/**
	 * This method is used to check reset password limit exceeded or not
	 * 
	 * @param username - user name fo the user
	 * @return boolean - response of limit exceeded as true or false
	 */
	@PostMapping(value = "/reset-password-limit-exceed/{username}")
	public SuccessResponse<Boolean> resetPasswordLimitExceed(
			@PathVariable(value = FieldConstants.USERNAME) String username) {
		Boolean response = userService.resetPasswordLimitExceed(username);
		return Boolean.TRUE.equals(response)
				? new SuccessResponse<>(SuccessCode.DISABLED_ACCOUNT, response, HttpStatus.OK)
				: new SuccessResponse<>(SuccessCode.PASSWORD_UPDATED, response, HttpStatus.OK);
	}
	
	/**
	 * This method is used to clear the api permission role map
	 * 
	 * @return SuccessResponse
	 */
	@GetMapping(value = "/clear")
	public SuccessResponse<Boolean> clearApiPermissions() {
		userService.clearApiPermissions();
		return new SuccessResponse<>(SuccessCode.API_PERMISSION_CLEARED, Constants.API_ROLES_MAP_CLEARED, HttpStatus.OK);
	}
}