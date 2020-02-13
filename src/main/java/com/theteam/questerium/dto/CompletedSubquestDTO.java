package com.theteam.questerium.dto;

import com.theteam.questerium.models.CompletedSubquest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CompletedSubquestDTO {
	QuestParticipantDTO user;
	SubquestDTO subquest;
	@NonNull String answer;
	boolean verified;

	public static CompletedSubquestDTO of(CompletedSubquest subquest) {
		return CompletedSubquestDTO.builder()
		                           .user(QuestParticipantDTO.of(subquest.getUser()))
		                           .subquest(SubquestDTO.of(subquest.getSubquest()))
		                           .answer(subquest.getAnswer())
		                           .verified(subquest.isVerified())
		                           .build();
	}
}
