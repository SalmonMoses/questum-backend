package com.theteam.questerium.responses;

import com.theteam.questerium.dto.QuestGroupOwnerDTO;
import lombok.Value;

@Value
public class OwnerSignupResponse {
	String token;
	String refreshToken;
	QuestGroupOwnerDTO owner;
	String error;

	public static OwnerSignupResponse ofError(String err) {
		return new OwnerSignupResponse("","", null, err);
	}
}
