package com.theteam.questum.responses;

import com.theteam.questum.dto.QuestGroupOwnerDTO;
import lombok.Value;

@Value
public class OwnerSignupResponse {
	String token;
	QuestGroupOwnerDTO owner;
	String error;

	public static OwnerSignupResponse ofError(String err) {
		return new OwnerSignupResponse("", null, err);
	}
}
