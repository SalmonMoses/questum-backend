package com.theteam.questum.requests;

import lombok.Value;

@Value
public class ParticipantLoginRequest {
	String refreshToken;
	Long groupId;
	String email;
	String password;
}
