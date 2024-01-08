package com.license.serviceImpl;

 

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;

import com.license.entity.Permission;
import com.license.entity.Role;
import com.license.entity.User;
import com.license.exception.CantDeleteException;
import com.license.repository.PermissionRepository;
import com.license.repository.RoleRepository;
import com.license.repository.UserRepository;
import com.license.service.RoleService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleRepository roleRepository;
	@Autowired
	private PermissionRepository permissionRepository;
	@Autowired
	private UserRepository userRepository;

	public Role getRoleByName(String roleName) {
		return roleRepository.findByName(roleName);
	}

	public List<Role> getAllRoles() {
		return roleRepository.findAll();
	}

	@Transactional
	public Boolean deleteRole(Long roleId) {
		Role roleToDelete = roleRepository.findById(roleId).orElse(null);

		if (roleToDelete != null) {
			// Find all users with the role to delete
			List<User> usersToDeleteRole = userRepository.findByRole(roleToDelete);

			// Get the "guest" role
			Role guestRole = roleRepository.findByName("guest");

			if (guestRole != null) {
				// Assign the "guest" role to users
				for (User user : usersToDeleteRole) {
					user.setRole(guestRole);
					userRepository.save(user);
				}
				try {
					roleRepository.deleteById(roleId);
				} catch (Exception e) {
					throw new CantDeleteException("Role can not be deleted..! Cause> " + e.getMessage());
				}

				return Boolean.TRUE;
			}

		}
		return Boolean.FALSE;
	}

	public Role createRoleWithPermissions(String roleName, List<String> permissions) {

		Role role = new Role();
		role.setName(roleName);

		List<Permission> addpermissions = permissionRepository.saveAll(createNewPermissions(permissions));

		for (Permission permission : addpermissions) {
			permission.setRole(role); // Establish the relationship
		}
		role.setPermission(addpermissions);
		return roleRepository.save(role);
	}

	public Role addPermissionsToRole(String roleName, List<String> permissionNames) {
		Role role = roleRepository.findByName(roleName);
		if (role == null) {
			log.info("the Role is not found");
			return null;
		}

		List<Permission> permissions = permissionRepository.saveAll(createNewPermissions(permissionNames));
		role.getPermission().addAll(permissions);

		return roleRepository.save(role);
	}

	private List<Permission> createNewPermissions(List<String> permissionNames) {
		List<Permission> newPermissions = new ArrayList<>();
		for (String permissionName : permissionNames) {
			Permission permission = new Permission();
			permission.setName(permissionName);
			newPermissions.add(permission);
		}
		return newPermissions;
	}

	@Transactional
	public Role updateRole(Long roleId, List<String> newPermissionNames) throws NotFoundException {
		Role role = roleRepository.findById(roleId).orElse(null);

		if (role == null) {
			throw new NotFoundException();
		}

		role.getPermission().clear();
		log.info("update role service method gets called ....");
		// Create a list of new permissions from the provided names
		for (String permissionName : newPermissionNames) {
			Permission permission = new Permission();
			permission.setName(permissionName);
			permission.setRole(role);
			role.getPermission().add(permission);
		}
		log.info("update role service method gets called .222...");
		// Save the updated role
		return roleRepository.save(role);

	}

	public List<String> getAllRolesName() throws NotFoundException {
		List<Role> roles = roleRepository.findAll();

		if (roles.isEmpty())
			throw new NotFoundException();
		List<String> rolesName = new ArrayList<String>();
		for (Role role : roles) {
			rolesName.add(role.getName());
		}

		return rolesName;
	}

	@Transactional
	public Boolean assignRoleToUser(String Email, String roleName) {
		// Find the user by their ID

		User user = userRepository.findByEmail(Email);
			

		// Find the role by its ID
		Role role = roleRepository.findByName(roleName);
		if (role == null)
			throw new EntityNotFoundException("Role not found with Name: " + roleName);

		// Assign the role to the user
		user.setRole(role);
		// Save the user with the assigned role
		User saveUser = userRepository.save(user);
		if (saveUser != null)
			return Boolean.TRUE;
		return Boolean.FALSE;
	}
 
}

//public Boolean addRoleToUser(String email, String roleName) {
//
//	Optional<User> user = userRepository.findByEmail(email);
//	Role role = roleRepository.findByName(roleName);
//	log.info("the fatched role is : " + role);
//	if (user.isPresent() && role != null) {
//		Role hasRoleToUser = roleRepository.findByUserId(user.get().getId());
//		if (hasRoleToUser != null) {
//			hasRoleToUser.setUser(null);
//			log.info("previous role is removed from user: " + role);
//		}
//
//		role.setUser(user.get());
//		Role savedRole = roleRepository.save(role);
//		if (savedRole != null) {
//			log.info("New Role assigned successfully to user id :" + email);
//			return Boolean.TRUE;
//		}
//	}
//	log.error("UserId   " + email + " or Role: " + roleName + " not exits in program");
//	return Boolean.FALSE;
//
//}