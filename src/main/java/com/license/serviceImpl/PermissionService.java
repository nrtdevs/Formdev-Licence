package com.license.serviceImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.license.entity.Permission;
import com.license.entity.Role;
import com.license.entity.User;
import com.license.repository.PermissionRepository;
import com.license.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class PermissionService {

	@Autowired
	private PermissionRepository permissionRepository;

	@Autowired
	UserRepository userRepository;

	@Transactional
	public void deleteRolesByUserId(long userId) throws Exception {
		try {
			permissionRepository.deleteRolesByUserId(userId);
		} catch (Exception e) {
			throw new Exception("Permission can not be deleted..! Cause> " + e.getMessage());
		}
		permissionRepository.deleteRolesByUserId(userId);
	}

	public List<String> getAllPermissionsList(long userId) {
		List<Permission> AllpermissionsList = permissionRepository.findByRoleId(userId);
		List<String> permissionList = new ArrayList<String>();
		for (Permission permissions : AllpermissionsList) {
			permissionList.add(permissions.getName());
		}
		return permissionList;
	}

	public List<String> getPermissionsByUserId(long l) {
		List<String> permissions = new ArrayList<>();

		User user = userRepository.findById((int) l).get();
		if (user != null) {
			// Get the user's role
			Role role = user.getRole();
			if (role != null) {
				// Get the permissions associated with the role
				List<Permission> rolePermissions = role.getPermission();
				for (Permission permission : rolePermissions) {
					permissions.add(permission.getName());
				}
			}
		}
		return permissions;
	}
}