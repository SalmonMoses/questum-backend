package com.theteam.questum.requests;

import lombok.Value;

import java.util.UUID;

@Value
public class LoginRequest {
	UUID refreshToken;
	String groupId;
	String email;
	String password;
}
