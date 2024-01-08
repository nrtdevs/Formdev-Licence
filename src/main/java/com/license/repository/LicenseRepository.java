package com.license.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.license.entity.License;
import java.util.List;
import java.util.Optional;


public interface LicenseRepository extends JpaRepository<License, Long> {

	 Optional<License> findById(Long id);

	List<License> findByType(String string);

	Optional<License> findByLicenseKey(String licenseKey);
	 
}

