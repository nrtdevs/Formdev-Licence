package com.license.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.List;
import java.util.UUID;

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
    private String selectedOptions;
    private int duration;
    private Date expirationDate;

    // MAC ID Based License specific fields
    private String macModuleName;       // For Module Based option
    private Integer macTenureDays;     // For Tenure Based option
    private Integer macUsageCount;     // For Count Based option

    // Email Based License specific fields
    private String specificEmail;      // For Email Specific option
    private String emailModuleName;    // For Module Specific option
    private Integer emailTenureDays;  // For Tenure Bound option
    private Integer emailWeeklyLimit;  // For Count Based Weekly Limit option

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

    // Helper method to get the appropriate option value based on license type
    public String getOptionValue() {
        if ("MAC_ID".equals(this.licenseType)) {
            switch (this.selectedOptions) {
                case "Module Based":
                    return this.macModuleName;
                case "Tenure Based":
                    return this.macTenureDays != null ? this.macTenureDays.toString() : "";
                case "Count Based":
                    return this.macUsageCount != null ? this.macUsageCount.toString() : "";
            }
        } else if ("EMAIL".equals(this.licenseType)) {
            switch (this.selectedOptions) {
                case "Email Specific":
                    return this.specificEmail;
                case "Module Specific":
                    return this.emailModuleName;
                case "Tenure Bound":
                    return this.emailTenureDays != null ? this.emailTenureDays.toString() : "";
                case "Count Based Weekly Limit":
                    return this.emailWeeklyLimit != null ? this.emailWeeklyLimit.toString() : "";
            }
        }
        return "";
    }
}