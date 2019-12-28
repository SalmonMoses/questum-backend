package com.theteam.questum.responses;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ParticipantCheckResponse {
	@NonNull
	String type;

	@NonNull
	long groupId;

	@NonNull
	String email;

	@NonNull
	boolean exists;
}
