package com.theteam.questum.responses;

import com.theteam.questum.dto.QuestGroupOwnerDTO;
import lombok.Value;

@Value
public class OwnerLoginResponse {
	private final String token;
	private final QuestGroupOwnerDTO owner;
}
