package com.license.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.license.entity.License;
import com.license.repository.UserRepository;
import com.license.service.LicenseService;
import com.license.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private LicenseService licenseService;

	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

//	@RequestMapping("/homePage1")
//	public String homePage() {
//		return"adminDashboard";
//	}

//	  @GetMapping("/homePage")
//	    public String adminPanel(Model model) {
//	        List<License> licenses = licenseService.getAllLicenses(); // Replace with your actual service method
//	        model.addAttribute("licenses", licenses);
//	        return "adminDashboard"; // This should match the name of your Thymeleaf template file
//	    }
	@GetMapping("/homePage")
	public String adminPanel(Model model) {

		String roleName = userRepository.findByEmail(userService.getCurrentUser()).getRole().getName();

		if ("Admin".equalsIgnoreCase(roleName)) {
			System.out.println("admin dashboard ");
			List<License> licenses = licenseService.getAllLicenses();
			int totalAtualUsers = licenseService.getTotalActualUsers(); // Replace with your actual service method

			int totalDemoUsers = licenseService.getTotalDemoUsers();

			model.addAttribute("licenses", licenses);
			model.addAttribute("totalAtualUsers", totalAtualUsers);
			model.addAttribute("totalDemoUsers", totalDemoUsers);

			return "adminDashboard";
		} else {
			System.out.println("user page  ");
			return "home";
		}
	}
	
	
	
	@GetMapping("/createLicense")
	public String createLincese(Model model) {
			return "home";

	}
	

}
