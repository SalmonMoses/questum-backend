package com.theteam.questum.responses;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SubmittedAnswerResponse {
	long userId;
	long subquestId;
	@NonNull String answer;
	boolean verified;
}
