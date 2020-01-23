package com.theteam.questerium.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class SubmitQuestAnswerRequest {
	long subquestId;

	@NonNull
	String answer;
}
