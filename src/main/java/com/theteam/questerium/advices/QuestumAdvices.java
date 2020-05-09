package com.theteam.questerium.advices;

import com.theteam.questerium.exceptions.GroupNotFoundException;
import com.theteam.questerium.responses.QuestumError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class QuestumAdvices {
	@ExceptionHandler(GroupNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String groupNotFoundHandler(GroupNotFoundException ex) {
		return ex.getMessage();
	}

	@ExceptionHandler(BadCredentialsException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	ResponseEntity<QuestumError> groupNotFoundHandler(BadCredentialsException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(QuestumError.builder()
		                                                                       .error("unauthorized")
		                                                                       .desc(ex.getMessage().toLowerCase())
		                                                                       .build());
	}
}
