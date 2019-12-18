package com.theteam.questum.responses;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class GroupCheckResponse {
	@NonNull
	String type;

	@NonNull
	long id;

	@NonNull
	boolean exists;
}
