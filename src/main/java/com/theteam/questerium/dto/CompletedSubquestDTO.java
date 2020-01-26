package com.theteam.questerium.dto;

import com.theteam.questerium.models.CompletedSubquest;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.models.Subquest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CompletedSubquestDTO {
	QuestParticipant user;
	Subquest subquest;
	@NonNull String answer;
	boolean verified;

	public static CompletedSubquestDTO of(CompletedSubquest subquest) {
		return CompletedSubquestDTO.builder()
		                           .user(subquest.getUser())
		                           .subquest(subquest.getSubquest())
		                           .answer(subquest.getAnswer())
		                           .verified(subquest.isVerified())
		                           .build();
	}
}
