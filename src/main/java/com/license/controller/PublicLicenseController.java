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

// @GetMapping("/check-validity/{licenseKey}")
// public ResponseEntity<Map<String, Object>> checkLicenseValidity(@PathVariable String licenseKey) {
//     Map<String, Object> response = new HashMap<>();

//     Optional<License> optionalLicense = licenseService.getLicenseByKey(licenseKey);
//     if (optionalLicense.isPresent()) {
//         License license = optionalLicense.get();
//         boolean isValid = new Date().before(license.getExpirationDate()) && new Date().after(license.getTimeStamp());

//         response.put("licenseKey", licenseKey);
//         response.put("status", isValid);
//         response.put("expirationDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(license.getExpirationDate()));

//         if (isValid) {
//             response.put("message", "License is valid");
//         } else {
//             response.put("message", "License expired");
//         }
//     } else {
//         response.put("licenseKey", licenseKey);
//         response.put("status", false);
//         response.put("message", "License not found");
//     }

//     return ResponseEntity.ok(response);
// }
@GetMapping("/check-validity/{licenseKey}")
public ResponseEntity<Boolean> checkLicenseValidity(@PathVariable String licenseKey) {

    Optional<License> optionalLicense = licenseService.getLicenseByKey(licenseKey);

    boolean isValid = false;

    if (optionalLicense.isPresent()) {
        License license = optionalLicense.get();
        isValid = new Date().before(license.getExpirationDate()) && new Date().after(license.getTimeStamp());
    }

    return ResponseEntity.ok(isValid); // sirf true ya false
}


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
