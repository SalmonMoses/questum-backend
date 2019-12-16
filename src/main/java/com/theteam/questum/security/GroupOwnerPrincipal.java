package com.theteam.questum.security;

import lombok.NonNull;
import lombok.Value;

import java.util.List;

@Value
public class GroupOwnerPrincipal {
	@NonNull
	String email;

	@NonNull
	String name;

	@NonNull
	List<Long> groups;
}
