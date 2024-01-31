package com.license.entity;

import java.sql.Date;
import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Entity
@Data
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String firstName;

	private String lastName;

	@Column(unique = true) // Ensure uniqueness in the database
	private String email;

	private String password;

	private Date passwordUpdatedAt;

	@Column(name = "account_creation_date")
	private LocalDate CreationDate;

	@ManyToOne
	@JoinColumn(name = "role_id")
	private Role role;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	private License license;

	public int Status;

	@PrePersist
	protected void onCreate() {
		CreationDate = LocalDate.now();
	}
}