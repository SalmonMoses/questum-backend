package com.theteam.questum.exceptions;

public class TokenExpiredException extends RuntimeException {
	public TokenExpiredException(String token) {
		super("Token has expired: " + token);
	}
}
