package com.project.telecounselor.userservice.repository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.stereotype.Repository;

import com.project.telecounselor.common.Constants;
import com.project.telecounselor.model.dto.UserDTO;
import com.project.telecounselor.model.entity.Role;
import com.project.telecounselor.model.entity.User;
import com.project.telecounselor.service.AuditService;
import com.project.telecounselor.util.UserContextHolder;

/**
 * <p>
 * This is the repository class for communicate link between server side and
 * database. This class used to perform all the user module action in database.
 * In query annotation (nativeQuery = true) the below query perform like SQL.
 * Otherwise its perform like HQL default value for nativeQuery FALSE
 * </p>
 * 
 * @author Prabu created on July 11, 2022
 */
@Repository
public class CustomRepositoryImpl implements JpaRepository<User, Long>, PagingAndSortingRepository<User, Long> {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private AuditService auditService;

	@Override
	public User save(User user) {
		User newUser = userRepository.save(user);
		try {
			auditData(user);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return newUser;
	}

	/**
	 * This method is used to audit the user data into database
	 * 
	 * @param user
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private void auditData(User user)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class firstClass = user.getClass();
		Method[] firstClassMethodsArr = firstClass.getMethods();
		for (int i = 0; i < firstClassMethodsArr.length; i++) {
			Method firstClassMethod = firstClassMethodsArr[i];
			if (firstClassMethod.getName().startsWith("get") && ((firstClassMethod.getParameterTypes()).length == 0)
					&& (!(firstClassMethod.getName().equals("getClass")))) {
				Object firstValue;
				firstValue = firstClassMethod.invoke(user, null);
				auditService.auditUserData(user, Constants.USER_ENTITY, Constants.CREATE,
						firstClassMethod.getName().substring(3, firstClassMethod.getName().length()), "",
						String.valueOf(firstValue));
			}
		}
	}

	/**
	 * 
	 * This method is used to audit the user data into database
	 * 
	 * @param user
	 * @param existingUser
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public void auditData(User user, User existingUser)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class firstClass = user.getClass();
		Method[] firstClassMethodsArr = firstClass.getMethods();
		Class secondClass = existingUser.getClass();
		Method[] secondClassMethodsArr = secondClass.getMethods();
		for (int i = 0; i < firstClassMethodsArr.length; i++) {
			Method firstClassMethod = firstClassMethodsArr[i];
			if (firstClassMethod.getName().startsWith("get") && ((firstClassMethod.getParameterTypes()).length == 0)
					&& (!(firstClassMethod.getName().equals("getClass")))) {
				Object firstValue;
				firstValue = firstClassMethod.invoke(existingUser, null);
				for (int j = 0; j < secondClassMethodsArr.length; j++) {
					Method secondClassMethod = secondClassMethodsArr[j];
					if (secondClassMethod.getName().equals(firstClassMethod.getName())) {
						Object secondValue = secondClassMethod.invoke(user, null);
						if (Objects.nonNull(secondValue) && (!secondValue.equals(firstValue))) {
							auditService.auditUserData(existingUser, Constants.USER_ENTITY, Constants.UPDATE,
									secondClassMethod.getName().substring(3, secondClassMethod.getName().length()),
									String.valueOf(firstValue), String.valueOf(secondValue));
						}
					}
				}
			}
		}
	}

	/**
	 * This method is used to save data
	 * 
	 * @param user
	 * @param existingUser
	 * @return
	 */
	public User save(User user, User existingUser) {
		try {
			auditData(user, existingUser);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return userRepository.save(user);
	}

	@Override
	public List<User> findAll() {
		return userRepository.findAll();
	}

	@Override
	public List<User> findAll(Sort sort) {
		return userRepository.findAll(sort);
	}

	@Override
	public List<User> findAllById(Iterable<Long> ids) {
		return userRepository.findAllById(ids);
	}

	@Override
	public <S extends User> List<S> saveAll(Iterable<S> entities) {
		return userRepository.saveAll(entities);
	}

	@Override
	public void flush() {
		userRepository.flush();

	}

	@Override
	public <S extends User> S saveAndFlush(S entity) {
		return userRepository.saveAndFlush(entity);
	}

	@Override
	public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) {
		return userRepository.saveAllAndFlush(entities);
	}

	@Override
	public void deleteAllInBatch(Iterable<User> entities) {
		userRepository.deleteAllInBatch();

	}

	@Override
	public void deleteAllByIdInBatch(Iterable<Long> ids) {
		userRepository.deleteAllByIdInBatch(ids);
	}

	@Override
	public void deleteAllInBatch() {
		userRepository.deleteAllInBatch();
	}

	@Override
	public User getOne(Long id) {
		return userRepository.getOne(id);
	}

	@Override
	public User getById(Long id) {
		return userRepository.getById(id);
	}

	@Override
	public User getReferenceById(Long id) {
		return userRepository.getReferenceById(id);
	}

	@Override
	public <S extends User> List<S> findAll(Example<S> example) {
		return userRepository.findAll(example);
	}

	@Override
	public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
		return userRepository.findAll(example, sort);
	}

	@Override
	public Page<User> findAll(Pageable pageable) {
		return userRepository.findAll(pageable);
	}

	@Override
	public Optional<User> findById(Long id) {
		return userRepository.findById(id);
	}

	@Override
	public boolean existsById(Long id) {
		return userRepository.existsById(id);
	}

	@Override
	public long count() {
		return userRepository.count();
	}

	@Override
	public void deleteById(Long id) {
		userRepository.deleteById(id);

	}

	@Override
	public void delete(User user) {
		userRepository.save(user);
		UserDTO userDto = UserContextHolder.getUserDto();
		auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.IS_ACTIVE, "true", "false");
		auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.UPDATED_BY,
				String.valueOf(user.getId()), String.valueOf(userDto.getId()));

	}

	@Override
	public void deleteAllById(Iterable<? extends Long> ids) {
		userRepository.deleteAllById(ids);

	}

	@Override
	public void deleteAll(Iterable<? extends User> entities) {
		userRepository.deleteAll();

	}

	@Override
	public void deleteAll() {
		userRepository.deleteAll();

	}

	@Override
	public <S extends User> Optional<S> findOne(Example<S> example) {
		return userRepository.findOne(example);
	}

	@Override
	public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
		return userRepository.findAll(example, pageable);
	}

	@Override
	public <S extends User> long count(Example<S> example) {
		return userRepository.count(example);
	}

	@Override
	public <S extends User> boolean exists(Example<S> example) {
		return userRepository.exists(example);
	}

	@Override
	public <S extends User, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
		return userRepository.findBy(example, queryFunction);
	}

	/**
	 * This method is used to save the updated password of the user
	 * 
	 * @param user
	 * @param initialPassword
	 * @param forgetPasswordToken
	 */
	public void updatePassword(User user, String oldPassword, String forgetPasswordToken) {
		userRepository.save(user);
		auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.FORGET_PASSWORD_TOKEN,
				forgetPasswordToken, user.getForgetPasswordToken());
		auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.PASSWORD, oldPassword,
				user.getPassword());
	}

	/**
	 * This method is used to save the user data on forget password
	 * 
	 * @param user
	 */
	public void forgetPassword(User user) {
		userRepository.save(user);
		auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.IS_BLOCKED, "false",
				"true");
		auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.IS_ACTIVE, "true", "false");
		auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.BLOCKED_DATE, "",
				user.getBlockedDate().toString());
		auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.FORGET_PASSWORD_COUNT, "5",
				String.valueOf(user.getForgetPasswordCount()));
	}

	/**
	 * This method is used to update the password token
	 * 
	 * @param user
	 * @param jwtToken
	 */
	public void updatePasswordToken(User user, String jwtToken, String forgetPasswordToken,
			int existingForgetPasswordCount) {
		userRepository.save(user);
		if (StringUtils.isEmpty(forgetPasswordToken)) {
			auditService.auditUserData(user, Constants.USER_ENTITY, Constants.CREATE, Constants.FORGET_PASSWORD_TOKEN,
					forgetPasswordToken, jwtToken);
		}
		if (existingForgetPasswordCount > 0) {
			auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.FORGET_PASSWORD_COUNT,
					String.valueOf(existingForgetPasswordCount), String.valueOf(existingForgetPasswordCount + 1));
		}
	}

	/**
	 * This method is used to save the updated password
	 * 
	 * @param user
	 * @param oldPassword
	 * @param newPassword
	 */
	public void changePassword(User user, String oldPassword, String newPassword) {
		userRepository.save(user);
		auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.PASSWORD, oldPassword,
				newPassword);
	}

	/**
	 * This method is used to delete all users with tenant id
	 * 
	 * @param tenantId
	 */
	@Transactional
	@Modifying
	public void deleteAllUserInTenant(long tenantId) {
		UserDTO userDTO = UserContextHolder.getUserDto();
		List<User> users = userRepository.getAllUserByTenantId(tenantId);
		for (User user : users) {
			String modifiedBy = String.valueOf(user.getUpdatedBy());
			user.setIsActive(false);
			user.setUpdatedBy(userDTO.getId());
			auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.IS_ACTIVE, "true",
					"false");
			auditService.auditUserData(user, Constants.USER_ENTITY, Constants.UPDATE, Constants.UPDATED_BY, modifiedBy,
					String.valueOf(user.getUpdatedBy()));
		}
		userRepository.saveAll(users);
	}

	/**
	 * this method is used to save role
	 * 
	 * @param role
	 * @return
	 */
	public Role saveRole(Role role) {
		Role newRole = roleRepository.save(role);
		try {
			auditRoleData(role);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return newRole;
	}

	/**
	 * 
	 * This method is used to audit role data into database
	 * 
	 * @param role
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private void auditRoleData(Role role)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class firstClass = role.getClass();
		Method[] firstClassMethodsArr = firstClass.getMethods();
		for (int i = 0; i < firstClassMethodsArr.length; i++) {
			Method firstClassMethod = firstClassMethodsArr[i];
			if (firstClassMethod.getName().startsWith("get") && ((firstClassMethod.getParameterTypes()).length == 0)
					&& (!(firstClassMethod.getName().equals("getClass")))) {
				Object firstValue;
				firstValue = firstClassMethod.invoke(role, null);
				auditService.auditRoleData(role, Constants.ROLE_ENTITY, Constants.CREATE,
						firstClassMethod.getName().substring(3, firstClassMethod.getName().length()), "",
						String.valueOf(firstValue));
			}
		}
	}

	/**
	 * This method is used to update role information
	 * 
	 * @param role
	 * @param existingRole
	 * @return
	 */
	public Role updateRole(Role role, Role existingRole) {
		try {
			auditRoleData(role, existingRole);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return roleRepository.save(role);

	}

	/**
	 * 
	 * This method is used to audit the user data into database
	 * 
	 * @param role
	 * @param existingRole
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	private void auditRoleData(Role role, Role existingRole)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class firstClass = role.getClass();
		Method[] firstClassMethodsArr = firstClass.getMethods();
		Class secondClass = existingRole.getClass();
		Method[] secondClassMethodsArr = secondClass.getMethods();
		for (int i = 0; i < firstClassMethodsArr.length; i++) {
			Method firstClassMethod = firstClassMethodsArr[i];
			if (firstClassMethod.getName().startsWith("get") && ((firstClassMethod.getParameterTypes()).length == 0)
					&& (!(firstClassMethod.getName().equals("getClass")))) {
				Object firstValue;
				firstValue = firstClassMethod.invoke(existingRole, null);
				for (int j = 0; j < secondClassMethodsArr.length; j++) {
					Method secondClassMethod = secondClassMethodsArr[j];
					if (secondClassMethod.getName().equals(firstClassMethod.getName())) {
						Object secondValue = secondClassMethod.invoke(role, null);
						if (Objects.nonNull(secondValue) && (!firstValue.equals(secondValue))) {
							auditService.auditRoleData(existingRole, Constants.ROLE_ENTITY, Constants.UPDATE,
									secondClassMethod.getName().substring(3, secondClassMethod.getName().length()),
									String.valueOf(firstValue), String.valueOf(secondValue));
						}
					}
				}
			}
		}
	}

	/**
	 * This method is used to delete role by id
	 * 
	 * @param roleId
	 * @param role
	 * @return
	 */
	public int deleteRoleById(long roleId, Role role) {
		int deletedRole = roleRepository.updateRoleStatusById(Boolean.FALSE, roleId);
		auditService.auditRoleData(role, Constants.ROLE_ENTITY, Constants.UPDATE, Constants.IS_ACTIVE, "true", "false");
		return deletedRole;
	}
}
