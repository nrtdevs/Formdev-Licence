package com.license.exception;

public class CantDeleteException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CantDeleteException(String message)
	{
		super(message);
	}
}
