package com.license.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Data
@ToString
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String companyName;
    private String filePath;
    private String macId;
    private String licenseKey;
    private String type;
    private String licenseFor;
    private String licenseType;

    private int duration;
    private Date expirationDate;

    private String phoneNumber;
    private String primaryContactName;
    private String primaryContactNumber;
    private String scmContactName;
    private String scmContactNumber;

    // MAC ID Based License specific fields
    private String macModuleName;       // For Module Based option
    private Integer macTenureDays;      // For Tenure Based option
    private Integer macUsageCount;      // For Count Based option

    // Email Based License specific fields
    private String specificEmail;       // For Email Specific option
    private String emailModuleName;     // For Module Specific option
    private Integer emailTenureDays;    // For Tenure Bound option
    private Integer weeklyLimit;        // For Count Based Weekly Limit option

    // Module expiry JSON stored in DB
    @Lob
    @Column(name = "module_expiry")
    private String moduleExpiryString;

    @Transient
    private Map<String, Integer> moduleExpiry;

    // ✅ Store modules as comma-separated string in DB, handle as List<String> in Java
    @Lob
    @Column(name = "modules")
    private String modulesString;

    @Transient
    private List<String> modules;

    public List<String> getModules() {
        if (this.modulesString != null && !this.modulesString.isEmpty()) {
            return Arrays.asList(this.modulesString.split(","));
        }
        return null;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
        if (modules != null) {
            this.modulesString = String.join(",", modules);
        } else {
            this.modulesString = null;
        }
    }

    private String userEmail;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

    @PrePersist
    public void prePersist() {
        // Generate a unique license key using UUID
        this.licenseKey = UUID.randomUUID().toString().toUpperCase();

        // Set the current timestamp
        this.timeStamp = new Date();

        // Calculate expiration date based on duration (days)
        if (this.duration > 0) {
            long durationInMillis = this.duration * 24L * 60 * 60 * 1000;
            this.expirationDate = new Date(System.currentTimeMillis() + durationInMillis);
        }
    }

    // ModuleExpiry getter and setter with JSON handling
    public Map<String, Integer> getModuleExpiry() {
        if (this.moduleExpiryString != null && !this.moduleExpiryString.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(this.moduleExpiryString, Map.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setModuleExpiry(Map<String, Integer> moduleExpiry) {
        this.moduleExpiry = moduleExpiry;
        if (moduleExpiry != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.moduleExpiryString = mapper.writeValueAsString(moduleExpiry);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else {
            this.moduleExpiryString = null;
        }
    }
}
