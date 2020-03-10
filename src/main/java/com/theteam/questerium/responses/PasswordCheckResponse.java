package com.theteam.questerium.responses;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PasswordCheckResponse {
	@NonNull
	boolean correct;
}
