package com.theteam.questum.responses;

import com.theteam.questum.dto.QuestGroupOwnerDTO;
import lombok.Value;

@Value
public class OwnerLoginResponse {
	String token;
	QuestGroupOwnerDTO owner;
}
