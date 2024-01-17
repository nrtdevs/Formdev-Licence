package com.license.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;
import lombok.ToString;

import java.util.Date;
import java.util.UUID;

import ch.qos.logback.core.subst.Token.Type;

@Entity
@ToString
@Data
public class License {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String email;

	@Column(unique = true) // Ensure uniqueness in the database
	private String macId;

	private String licenseKey;

	private int duration;

	private Date expirationDate;

	private String type;
	
	private String companyName;

	@Temporal(TemporalType.TIMESTAMP)
	private Date timeStamp;

	@PrePersist
	public void prePersist() {
		// Generate a unique license key using UUID
		this.licenseKey = UUID.randomUUID().toString().toUpperCase();

		// Set the current timestamp
		this.timeStamp = new Date();

	}

}
