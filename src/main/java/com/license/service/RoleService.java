package com.license.service;

import java.util.List;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

import com.license.entity.Role;

public interface RoleService {

	public Role getRoleByName(String roleName);

	public List<Role> getAllRoles();

	public Boolean deleteRole(Long roleId);

	public Role createRoleWithPermissions(String roleName, List<String> permissions);

	public Role addPermissionsToRole(String roleName, List<String> permissionNames);

	public Role updateRole(Long roleId, List<String> newPermissionNames) throws NotFoundException;

	public List<String> getAllRolesName() throws NotFoundException;

	public Boolean assignRoleToUser(String Email, String roleName);
}
