package com.theteam.questerium.security;

import lombok.NonNull;
import lombok.Value;

@Value
public class ParticipantPrincipal implements IUserPrincipal {
	@NonNull
	Long id;

	@NonNull
	String email;

	@NonNull
	String name;

	@NonNull
	Long group;
}
