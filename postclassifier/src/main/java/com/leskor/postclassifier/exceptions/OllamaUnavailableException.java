package com.leskor.postclassifier.exceptions;

public class OllamaUnavailableException extends RuntimeException {
	public OllamaUnavailableException(String message) {
		super(message);
	}

	public OllamaUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
