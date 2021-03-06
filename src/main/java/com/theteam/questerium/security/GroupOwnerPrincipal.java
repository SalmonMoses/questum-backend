package com.theteam.questerium.security;

import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
public class GroupOwnerPrincipal implements IUserPrincipal {
	@NonNull
	String email;

	@NonNull
	String name;

	@NonNull
	List<Long> groups;
}
