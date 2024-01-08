package com.license.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.license.entity.License;
import com.license.entity.User;
import com.license.security.LoginRequest;
import com.license.service.UserService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequestMapping("/users")
public class UserController {

	@Autowired
	private UserService userService;

//	@GetMapping("/getAllUsers")
//	public List<User> getAllUsers() {
//		return userService.getAllUsers();
//	}

	@GetMapping("/getAllUsers")
	@PreAuthorize("hasRole('ADMIN')  or hasRole('ROLE_USER_LIST')")
	public String getAllUsers(Model model) {
		List<User> users = userService.getAllUsers();
		model.addAttribute("users", users);
		return "usersList";
	}

	@GetMapping("/{id}")
	 
	public User getUserById(@PathVariable int id) {
		return userService.getUserById(id);
	}

	@PostMapping("/createUser")
	public ResponseEntity<User> createUser(@RequestBody User user, HttpServletRequest request) {

		String url = request.getRequestURL().toString();
		url = url.replace(request.getServletPath(), "");
		User createUser = userService.createUser(user, url);

		if (createUser != null) {
			return new ResponseEntity<>(createUser, HttpStatus.OK);
		} else {
			// You can customize the response based on your requirements
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/{id}")
	public User updateUser(@PathVariable int id, @RequestBody User updatedUser) {
		return userService.updateUser(id, updatedUser);
	}

//	@PostMapping("/loginUser")
//	public ResponseEntity<User> loginUser(@RequestParam String email, @RequestParam String password) {
//	    User loggedInUser = userService.loginUser(email, password);
//
//	    if (loggedInUser != null) {
//	        return new ResponseEntity<>(loggedInUser, HttpStatus.OK);
//	    } else {
//	        // You can customize the response based on your requirements
//	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // or HttpStatus.NOT_FOUND, etc.
//	    }
//	}
//	

//	@PostMapping("/loginUser")
//	public ResponseEntity<User> loginUser(@RequestBody User loginCredentials) {
//	    String email = loginCredentials.getEmail();
//	    String password = loginCredentials.getPassword();
//
//	    User loggedInUser = userService.loginUser(email, password);
//
//	    if (loggedInUser != null) {
//	        return new ResponseEntity<>(loggedInUser, HttpStatus.OK);
//	    } else {
//	        // You can customize the response based on your requirements
//	        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED); // or HttpStatus.NOT_FOUND, etc.
//	    }
//	}

	@PostMapping("/loginUser")
	public ResponseEntity<User> loginUser(@RequestBody LoginRequest loginCredentials, HttpServletRequest request) {
		String email = loginCredentials.getEmail();
		String password = loginCredentials.getPassword();
     System.out.println("user data : "+loginCredentials);
		User loggedInUser = userService.loginUser(email, password);

		if (loggedInUser != null) {
			// Store user information in the session
			HttpSession session = request.getSession();
			session.setAttribute("loggedInUser", loggedInUser);

			return new ResponseEntity<>(loggedInUser, HttpStatus.OK);
		} else {
			// You can customize the response based on your requirements
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			System.out.println("user logged out " + session.getAttribute("loggedInUser"));
			session.invalidate(); // Invalidate the session

			return "login"; // Redirect to the login page or any other page
		}
		return "home";
	}

	@DeleteMapping("/{id}")
	public void deleteUser(@PathVariable int id) {
		userService.deleteUser(id);
	}

	// this is front page end-point

	@RequestMapping("/registration")
	public String registration() {

		return "createUser";
	}

	@RequestMapping("/loginUser")
	public String loginUser() {

		return "login";
	}

	@RequestMapping("/home")
	public String home() {

		return "home";

	}

	@RequestMapping("/forgotPage")
	public String forgotPage() {
		return "forgotPassword";

	}

	@RequestMapping("/updatePasswordPage")
	public String updatePasswordPage() {
		return "updatePassword";

	}

	@PostMapping("/forgot")
	public String generateOtp(@RequestParam("email") String email, HttpServletRequest request)
			throws MessagingException {
		request.getSession().setAttribute("email", email);
		userService.generateOtp(email);
		return "varifyPage";
	}

	@PostMapping("/verifyOtp")
	public String verifyOtp(@RequestParam("otp") String enteredOtp, HttpServletRequest request) {
		HttpSession session = request.getSession();
		System.out.print("the otp is : " + enteredOtp);

		boolean isOtpValid = userService.verifyOtp(session, enteredOtp);

		if (isOtpValid) {
			// OTP is valid, you can proceed with the desired action
			return "newPasswordPage";
		} else {
			// Invalid OTP, return an error response
			return "failed to set the password";
		}
	}

	@PostMapping("/updatePassword")
	public String resetPassword(HttpServletRequest request, @RequestParam("newpassword") long newPassword,
			@RequestParam("password") long password) {
		String currentUser = (String) request.getSession().getAttribute("email");
		if (newPassword == password) {
			Boolean isUpdated = userService.updatePassword(password, currentUser);
			if (isUpdated)
				return "login";
			else
				return "failed to update the password";
		} else
			return "password mismatch";
	}

	@PostMapping("/resetPassword")
	public String updatePassword(@RequestParam("email") String currentUser,
			@RequestParam("newpassword") long newPassword, @RequestParam("currentpassword") long currentPassword,
			@RequestParam("password") long confirmPassword) {

		System.out.println("controller called");
		
		if (newPassword == confirmPassword) {
			System.out.println("passwords are equals");
			Boolean isUpdated = userService.resetPassword(currentPassword, currentUser, newPassword);
			if (isUpdated)
				return "login";
			else
				return "failed to update the password";
		} else
			return "password mismatch";
	}

}
