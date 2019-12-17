package com.theteam.questum.advices;

import antlr.Token;
import com.theteam.questum.exceptions.GroupNotFoundException;
import com.theteam.questum.exceptions.TokenExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.http.HttpResponse;

@ControllerAdvice
public class TokenExpiredAdvice {

}
