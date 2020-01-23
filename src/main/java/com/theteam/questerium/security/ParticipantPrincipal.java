package com.theteam.questerium.security;

import lombok.NonNull;
import lombok.Value;

@Value
public class ParticipantPrincipal {
	@NonNull
	String email;

	@NonNull
	String name;

	@NonNull
	Long group;
}
