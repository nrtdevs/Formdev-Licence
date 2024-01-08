package com.license.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.license.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

	List<Permission> findByRoleId(long roleId);

	List<Permission> findByNameIn(List<String> permissions);

	List<Permission> findByRoleName(String roleName);
	

	@Modifying
    @Query(value = "DELETE FROM role WHERE user_id = :userId", nativeQuery = true)
    void deleteRolesByUserId(long userId);

	boolean existsByRoleName(String string);

	
	
}
