package com.license.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.*;
import java.util.UUID;
import java.util.Arrays;

import com.fasterxml.jackson.core.type.TypeReference;
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
    private String macModuleName;
    private Integer macTenureDays;
    private Integer macUsageCount;

    // Email Based License specific fields
    private String specificEmail;
    private String emailModuleName;
    private Integer emailTenureDays;
    private Integer weeklyLimit;

    /*
     * =========================================================
     * MODULES (comma-separated string in DB)
     * =========================================================
     */

    @Lob
    @Column(name = "modules", columnDefinition = "LONGTEXT")
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
        if (modules != null && !modules.isEmpty()) {
            this.modulesString = String.join(",", modules);
        } else {
            this.modulesString = null;
        }
    }

    /*
     * =========================================================
     * MODULE EXPIRY (JSON stored as LONGTEXT)
     * =========================================================
     */

    @Lob
    @Column(name = "module_expiry", columnDefinition = "LONGTEXT")
    private String moduleExpiryString;

    @Transient
    private Map<String, List<String>> moduleExpiry;

    public Map<String, List<String>> getModuleExpiry() {
        if (this.moduleExpiryString != null && !this.moduleExpiryString.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(
                        this.moduleExpiryString,
                        new TypeReference<Map<String, List<String>>>() {
                        });
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse moduleExpiry JSON", e);
            }
        }
        return null;
    }

    public void setModuleExpiry(Map<String, List<String>> moduleExpiry) {
        this.moduleExpiry = moduleExpiry;
        if (moduleExpiry != null && !moduleExpiry.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.moduleExpiryString = mapper.writeValueAsString(moduleExpiry);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize moduleExpiry JSON", e);
            }
        } else {
            this.moduleExpiryString = null;
        }
    }

    /*
     * =========================================================
     * OTHER FIELDS
     * =========================================================
     */

    private String userEmail;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_modified_at")
    @com.fasterxml.jackson.annotation.JsonProperty("last_modified_at")
    private Date lastModifiedAt;

    /*
     * =========================================================
     * JPA LIFECYCLE HOOKS
     * =========================================================
     */

    @PrePersist
    public void prePersist() {
        this.licenseKey = UUID.randomUUID().toString().toUpperCase();
        this.timeStamp = new Date();

        if (this.duration > 0) {
            long durationInMillis = this.duration * 24L * 60 * 60 * 1000;
            this.expirationDate = new Date(System.currentTimeMillis() + durationInMillis);
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.lastModifiedAt = new Date();
    }
}
