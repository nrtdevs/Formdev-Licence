package com.license.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.license.entity.User;
import com.license.security.ApiResponse;
import com.license.security.LoginRequest;
import com.license.security.LoginResponse;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

public interface UserService {

	User getUserById(int id);

	User createUser(User user, String url);

	User updateUser(int id, User updatedUser);

	void deleteUser(int id);

	User loginUser(String email, String password);

	List<User> getAllUsers();

	String generateOtp(String email) throws MessagingException;

	void sendOtpEmail(String email, String otp) throws MessagingException;

	boolean verifyOtp(HttpSession session, String enteredOtp);

	Boolean updatePassword(long password, String currentUser);


	ResponseEntity<ApiResponse<LoginResponse>> generateToken(LoginRequest loginRequest);

	public void assignDefaultRoleToUser(String email);

	String getCurrentUser();

	Boolean resetPassword(long currentPassword, String currentUser, long newPassword);

	Boolean isPasswordOlderThan3Months(String userEmail);
}
