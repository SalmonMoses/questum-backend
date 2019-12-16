package com.theteam.questum.requests;

import lombok.NonNull;
import lombok.Value;

@Value
public class SignupRequest {
	@NonNull
	String email;

	@NonNull
	String name;

	@NonNull
	String password;
}
