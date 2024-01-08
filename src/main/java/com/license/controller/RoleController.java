package com.license.controller;

 
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.license.entity.Role;
import com.license.serviceImpl.PermissionService;
import com.license.serviceImpl.RoleServiceImpl;
 

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

	private final RoleServiceImpl roleService;
	@Autowired
	PermissionService permissionService;

	public RoleController(RoleServiceImpl roleService) {
		this.roleService = roleService;
	}

	@PostMapping("/createrole")
	public ModelAndView createRole(ModelAndView modelAndView, @RequestParam("roleName") String RoleName,
			@RequestParam("permissions") List<String> permissions) {
		Role role = roleService.createRoleWithPermissions(RoleName, permissions);
		if (role != null)
			modelAndView.addObject("message", "Role is created successfully....");
		else
			modelAndView.addObject("message", "Failed to create the Role: " + RoleName + "...!!! ");
		modelAndView.setViewName("/html/RolePermissions/response-permissions");
		return modelAndView;

	}

	@GetMapping("/roles")
	public ModelAndView getAllRoles(ModelAndView model) {
		List<Role> roles = roleService.getAllRoles();
		model.addObject("roles", roles);
		return model;
	}

	@GetMapping("/delete-role")
	public ModelAndView deleteRole(@RequestParam("id") Long roleId, ModelAndView modelAndView) {
		Boolean isDeleted = roleService.deleteRole(roleId);
		if (isDeleted) {
			List<Role> roles = roleService.getAllRoles();
			modelAndView.addObject("roles", roles);
			modelAndView.setViewName("/html/RolePermissions/roles-list");
		} else {
			modelAndView.addObject("message", "Failed to deleted the Role: " + roleId);
			modelAndView.setViewName("/html/RolePermissions/response-permissions");
		}
		return modelAndView;
	}

	@GetMapping("/roles-list/page")
	public ModelAndView getRolles(ModelAndView modelAndView) {
		List<Role> roles = roleService.getAllRoles();
		modelAndView.addObject("roles", roles);
		modelAndView.setViewName("/html/RolePermissions/roles-list");
		return modelAndView;
	}

	@PostMapping("/update-role")
	public ModelAndView updateRole(@RequestParam("id") Long roleId,
			@RequestParam("permissions") List<String> newPermissions, ModelAndView modelAndView) {
		try {
			Role role = roleService.updateRole(roleId, newPermissions);
			if (role != null)
				modelAndView.addObject("message", "Role is updated successfully..");
			else
				modelAndView.addObject("message", "Failed to update the Role ..!!!");
		} catch (Exception e) {
			modelAndView.addObject("message",
					" Exeption Occoured Failed to update the Role ..!!!" + e.getMessage().toString());
		}
		modelAndView.setViewName("/html/RolePermissions/response-permissions");
		return modelAndView;
	}

	@GetMapping("/update-role/page")
	public ModelAndView getUpdateRolePage(@RequestParam("id") long roleId, @RequestParam("name") String roleName,
			ModelAndView modelAndView) {
		List<String> userPermissions = permissionService.getAllPermissionsList(roleId);
		modelAndView.addObject("userPermissions", userPermissions);
		modelAndView.addObject("id", roleId);
		modelAndView.addObject("name", roleName);
		modelAndView.setViewName("/html/RolePermissions/update-permissions");
		return modelAndView;
	}

	@GetMapping("/assign-roles/page")
	public ModelAndView AssignRoleToUserPage(ModelAndView modelAndView) {
		List<String> rolesName = null;
		try {
			rolesName = roleService.getAllRolesName();
			modelAndView.addObject("roles", rolesName);
		} catch (NotFoundException e) {
			modelAndView.addObject("message", "Role list is Empty");
			modelAndView.setViewName("/html/RolePermissions/response-permissions");
			e.printStackTrace();
		}

		modelAndView.setViewName("/html/RolePermissions/AssignRole");
		return modelAndView;
	}

	@PostMapping("/assign-role")
	public ModelAndView AssignRoleToUser(@RequestParam("email") String email, @RequestParam("roleName") String roleName,
			ModelAndView modelAndView) {
		try {
			Boolean isAssigned = roleService.assignRoleToUser(email, roleName);
			if (isAssigned)
				modelAndView.addObject("message", "Role " + roleName + " is assigned to user :" + email);
			else
				modelAndView.addObject("message", " Failed to assign the Role to user :" + email);
		} catch (Exception e) {
			modelAndView.addObject("message", "Failed to assign the Role to user :" + email
					+ "Because Exception Occourred" + e.getMessage().toString());
		}
		modelAndView.setViewName("/html/RolePermissions/response-permissions");
		return modelAndView;
	}

	@GetMapping("/create-role/page")
	public ModelAndView PermissionHomePage(ModelAndView modelAndView) {
		modelAndView.setViewName("/html/RolePermissions/create-role");
		return modelAndView;

	}
}
