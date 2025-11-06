package com.license.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.entity.License;
import com.license.service.LicenseService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RequestMapping("/public") // सभी public APIs
public class PublicLicenseController {

    @Autowired
    private LicenseService licenseService;

// @GetMapping("/check-validity/{licenseKey}")
// public ResponseEntity<Map<String, Object>> checkLicenseValidity(@PathVariable String licenseKey) {
//     Map<String, Object> response = new HashMap<>();





@GetMapping("/check-validity/{licenseKey}")
public ResponseEntity<Map<String, Object>> checkLicenseValidity(@PathVariable String licenseKey) {
    Map<String, Object> response = new HashMap<>();

    Optional<License> optionalLicense = licenseService.getLicenseByKey(licenseKey);
    if (!optionalLicense.isPresent()) {
        response.put("status", false);
        response.put("message", "License not found");
        return ResponseEntity.ok(response);
    }

    License license = optionalLicense.get();

    try {
        // ✅ Parse moduleExpiryString JSON
        ObjectMapper mapper = new ObjectMapper();

        Map<String, List<String>> moduleMap =
                mapper.readValue(license.getModuleExpiryString(), new TypeReference<Map<String, List<String>>>() {});

        boolean atLeastOneModuleValid = false;
        Date today = new Date();

        for (Map.Entry<String, List<String>> entry : moduleMap.entrySet()) {

            String moduleName = entry.getKey();
            int validityDays = Integer.parseInt(entry.getValue().get(0));  // e.g. "50"
            String creationDateStr = entry.getValue().get(1);              // e.g. "2025-11-06"

            // ✅ Convert creation date string to Date
            Date creationDate = new SimpleDateFormat("yyyy-MM-dd").parse(creationDateStr);

            // ✅ expiryDate = creationDate + validityDays
            Calendar cal = Calendar.getInstance();
            cal.setTime(creationDate);
            cal.add(Calendar.DAY_OF_MONTH, validityDays);
            Date moduleExpiry = cal.getTime();

            // ✅ Check module validity
            if (today.before(moduleExpiry)) {
                atLeastOneModuleValid = true;
                break; // ✅ license valid if ANY module valid
            }
        }

        // ✅ Final license status
        if (atLeastOneModuleValid) {
            response.put("status", true);
            response.put("message", "License is valid");
            response.put("license", license);  // SAME RESPONSE
        } else {
            response.put("status", false);
            response.put("message", "License expired or invalid");
        }

    } catch (Exception e) {
        response.put("status", false);
        response.put("message", "Invalid module expiry data");
    }

    return ResponseEntity.ok(response);
}





@GetMapping("/check-license/{licenseKey}")
public ResponseEntity<Map<String, Object>> checkLicense(@PathVariable String licenseKey) {
    return checkLicenseValidity(licenseKey);
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
