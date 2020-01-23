package com.theteam.questerium.responses;

import com.theteam.questerium.dto.QuestGroupOwnerDTO;
import lombok.Value;

@Value
public class OwnerLoginResponse {
	String token;
	String refreshToken;
	QuestGroupOwnerDTO owner;
}
