package com.theteam.questerium.dto;

import com.theteam.questerium.models.CompletedSubquest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CompletedSubquestDTO {
	long id;
	QuestParticipantDTO user;
	SubquestDTO subquest;
	@NonNull String answer;
	boolean verified;

	public static CompletedSubquestDTO of(CompletedSubquest completedSubquest) {
		return CompletedSubquestDTO.builder()
		                           .id(completedSubquest.getId())
		                           .user(QuestParticipantDTO.of(completedSubquest.getUser()))
		                           .subquest(SubquestDTO.of(completedSubquest.getSubquest()))
		                           .answer(completedSubquest.getAnswer())
		                           .verified(completedSubquest.isVerified())
		                           .build();
	}
}
