package com.license.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.license.entity.License;
import com.license.service.LicenseService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/public") // सभी public APIs
public class PublicLicenseController {

    @Autowired
    private LicenseService licenseService;

@GetMapping("/check-validity/{licenseKey}")
public ResponseEntity<Map<String, Object>> checkLicenseValidity(@PathVariable String licenseKey) {
    Map<String, Object> response = new HashMap<>();

    Optional<License> optionalLicense = licenseService.getLicenseByKey(licenseKey);
    boolean isValid = false;

    if (optionalLicense.isPresent()) {
        License license = optionalLicense.get();
        isValid = new Date().before(license.getExpirationDate()) && new Date().after(license.getTimeStamp());

        // Always include these
        response.put("companyName", license.getCompanyName());
        response.put("licenseFor", license.getLicenseFor());
        response.put("licenseType", license.getLicenseType());
        response.put("duration", license.getDuration());
        // response.put("expirationDate", license.getExpirationDate() != null ? license.getExpirationDate().getTime() : null);
        response.put("message", isValid ? "License is valid" : "License expired");
        response.put("status", isValid);
        

        response.put("createdAt", license.getTimeStamp());
        response.put("modifiedAt", license.getModifiedAt());

        // Conditional fields
        if ("EMAIL_ID".equals(license.getLicenseType())) {
            response.put("specific_email", license.getUserEmail());
            response.put("weeklyLimit", license.getWeeklyLimit());
            response.put("modules", license.getModules());
            response.put("moduleExpiry", license.getModuleExpiry());
        } else if ("MAC_ID".equals(license.getLicenseType())) {
            response.put("MAC_ID", license.getMacId());
            response.put("modules", license.getModules());
            response.put("macUsageCount", license.getMacUsageCount());
            response.put("moduleExpiry", license.getModuleExpiry());
        }

    } else {
        response.put("companyName", null);
        response.put("licenseFor", null);
        response.put("licenseType", null);
        response.put("duration", null);
        response.put("expirationDate", null);
        response.put("message", "License not found");
        response.put("status", false);
    }

    return ResponseEntity.ok(response);
}


// @GetMapping("/check-validity/{licenseKey}")
// public ResponseEntity<Boolean> checkLicenseValidity(@PathVariable String licenseKey) {

//     Optional<License> optionalLicense = licenseService.getLicenseByKey(licenseKey);

//     boolean isValid = false;

//     if (optionalLicense.isPresent()) {
//         License license = optionalLicense.get();
//         isValid = new Date().before(license.getExpirationDate()) && new Date().after(license.getTimeStamp());
//     }

//     return ResponseEntity.ok(isValid); // sirf true ya false
// }


//  @GetMapping("/all-licenses")
//     public ResponseEntity<Map<String, Object>> getAllLicenses() {
//         Map<String, Object> response = new HashMap<>();
//         try {
//             List<License> licenses = licenseService.getAllLicenses();
//             response.put("status", true);
//             response.put("total", licenses.size());
//             response.put("licenses", licenses);
//         } catch (Exception e) {
//             response.put("status", false);
//             response.put("message", "Error fetching licenses: " + e.getMessage());
//         }
//         return ResponseEntity.ok(response); // हमेशा 200
//     }


}
