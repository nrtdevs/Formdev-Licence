package com.license.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class LoginRequest {
	private String email;
	private String password;

	// Default constructor
	public LoginRequest() {
	}

	// Constructor with email and password
	public LoginRequest(String email, String password) {
		this.email = email;
		this.password = password;
	}
}