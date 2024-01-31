package com.license.repository;

import com.license.entity.Role;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Integer> {

	Optional<Role> findById(Long id);

	void deleteById(Long id);

	Role findByName(String string);

}
