package com.license.exception;

import org.springframework.security.authentication.InternalAuthenticationServiceException;

public class DeactivatedUserException extends InternalAuthenticationServiceException {
	private static final long serialVersionUID = 1L;

	public DeactivatedUserException(String message) {
		super(message);
	}
}
