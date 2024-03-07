package com.leskor.postclassifier.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(OllamaUnavailableException.class)
	public ResponseEntity<String> handleOllamaUnavailableException(OllamaUnavailableException e) {
		logger.error("Cannot query Ollama", e);
		return new ResponseEntity<>(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
	}
}
