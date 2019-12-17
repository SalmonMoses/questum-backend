package com.theteam.questum.advices;

import com.theteam.questum.exceptions.GroupNotFoundException;
import com.theteam.questum.exceptions.TokenExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class QuestumAdvices {
	@ExceptionHandler(GroupNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	String groupNotFoundHandler(GroupNotFoundException ex) {
		return ex.getMessage();
	}

	@ExceptionHandler(TokenExpiredException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	String tokenHasExpiredHandler(TokenExpiredException ex) {
		return ex.getMessage();
	}
}
