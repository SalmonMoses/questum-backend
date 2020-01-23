package com.theteam.questerium.requests;

import lombok.Value;

@Value
public class ParticipantLoginRequest {
	String refreshToken;
	Long groupId;
	String email;
	String password;
}
