package com.license.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.license.entity.Role;
import com.license.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

	User findByEmailAndPassword(String email, String password);

	User findByEmail(String currentUser);

	boolean existsByEmail(String string);

	List<User> findByRole(Role roleToDelete);

	
	
}