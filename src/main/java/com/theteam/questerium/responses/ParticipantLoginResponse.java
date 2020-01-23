package com.theteam.questerium.responses;

import com.theteam.questerium.dto.QuestParticipantDTO;
import lombok.Value;

@Value
public class ParticipantLoginResponse {
	String token;
	String refreshToken;
	QuestParticipantDTO user;
}
