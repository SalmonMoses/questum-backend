package com.theteam.questerium.requests;

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
