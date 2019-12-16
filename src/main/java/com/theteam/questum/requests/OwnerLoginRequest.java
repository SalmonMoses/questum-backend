package com.theteam.questum.requests;

import lombok.Value;

@Value
public class OwnerLoginRequest {
	String refreshToken;
	String groupId;
	String email;
	String password;
}
