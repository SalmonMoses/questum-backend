package com.theteam.questum.responses;

import com.theteam.questum.dto.QuestParticipantDTO;
import lombok.Value;

@Value
public class ParticipantLoginResponse {
	String token;
	String refreshToken;
	QuestParticipantDTO user;
}
