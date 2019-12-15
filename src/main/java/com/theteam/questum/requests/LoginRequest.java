package com.theteam.questum.requests;

import lombok.Value;

@Value
public class LoginRequest {
	String refreshToken;
	String groupId;
	String email;
	String password;
}
