package com.license.service;

import java.util.List;
import java.util.Optional;

import com.license.entity.License;

import jakarta.servlet.http.HttpSession;

public interface LicenseService {

	List<License> getAllLicenses();

	Optional<License> getLicenseById(Long id);

	License updateLicense(Long id, License updatedLicense);

	void deleteLicense(Long id);

	License createLicense(License license, HttpSession session);

	int getTotalDemoUsers();

	int getTotalActualUsers();

	List<License> getAllDemoLicenses();

	List<License> getAllActualLicenses();

	boolean isValidLicenseKey(String licenseKey);

	boolean isLicenseValid(String licenseKey);

	License findLicenseBySearchInput(String searchInput);
	
	Optional<License> getLicenseByKey(String licenseKey);

	org.springframework.core.io.Resource getEncryptedLicenseFile(Long id) throws Exception;

}
