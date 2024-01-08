package com.license.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.license.entity.License;
import com.license.service.LicenseService;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/licenses")
public class LicenseController {

	@Autowired
	private LicenseService licenseService;

	@Autowired
	public LicenseController(LicenseService licenseService) {
		this.licenseService = licenseService;
	}

//    @GetMapping("/getAllLicenses")
//    public List<License> getAllLicenses() {
//        return licenseService.getAllLicenses();
//    }
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/getAllLicenses")
	public String getAllLicenses(Model model) {
		List<License> licenses = licenseService.getAllLicenses();
		model.addAttribute("licenses", licenses);
		return "licensesList";
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public Optional<License> getLicenseById(@PathVariable Long id) {
		return licenseService.getLicenseById(id);
	}

	@PreAuthorize("hasRole('ADMIN')  or hasRole('ROLE_BUY_ACTUAL_LICENSE') or hasRole('ROLE_BUY_DEMO_LICENSE') ")
	@PostMapping("/createLicense")
	public ResponseEntity<License> createLicense(@RequestBody License license, HttpSession session) {

		System.out.print(license);
		License createdLicense = licenseService.createLicense(license, session);

		if (createdLicense != null) {
			return new ResponseEntity<>(createdLicense, HttpStatus.OK);
		} else {
			// You can customize the response based on your requirements
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')  or hasRole('ROLE_UPDATE_ACTUAL_LICENSE')")
	public License updateLicense(@PathVariable Long id, @RequestBody License updatedLicense) {
		return licenseService.updateLicense(id, updatedLicense);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')  or hasRole('ROLE_DELETE_LICENSE')")
	public void deleteLicense(@PathVariable Long id) {
		licenseService.deleteLicense(id);
	}

	@GetMapping("/newLicence")
	@PreAuthorize("hasRole('ADMIN')  or hasRole('ROLE_BUY_ACTUAL_LICENSE')")
	public String createLicense() {
		return "createLicense";
	}

	@GetMapping("/demoLicense")
	@PreAuthorize("hasRole('ADMIN')  or hasRole('ROLE_BUY_DEMO_LICENSE')")
	public String demoLicense() {
		return "demoLicense";
	}

	@GetMapping("/demoLicenseList")
	@PreAuthorize("hasRole('ADMIN')")
	public String demoLicenseList(Model model) {
		List<License> licenses = licenseService.getAllDemoLicenses();
		model.addAttribute("licenses", licenses);
		return "demoLicenseList";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/actualLicenseList")
	public String actualLicenseList(Model model) {
		List<License> licenses = licenseService.getAllActualLicenses();
		model.addAttribute("licenses", licenses);
		return "actualLicenseList";
	}

	// check licenseKey valid or not
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/validateKey/{licenseKey}")
	public ResponseEntity<Boolean> validateLicenseKey(@PathVariable String licenseKey) {
		boolean isValid = licenseService.isValidLicenseKey(licenseKey);
		return ResponseEntity.ok(isValid);
	}

	// check license is valid or not
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/validateLicense/{licenseKey}")
	public ResponseEntity<Boolean> checkLicenseValidity(@PathVariable String licenseKey) {
		boolean isValid = licenseService.isLicenseValid(licenseKey);
		return ResponseEntity.ok(isValid);
	}
}
