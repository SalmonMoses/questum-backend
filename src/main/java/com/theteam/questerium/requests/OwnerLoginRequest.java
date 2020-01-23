package com.theteam.questerium.requests;

import lombok.Value;

@Value
public class OwnerLoginRequest {
	String refreshToken;
	String email;
	String password;
}
