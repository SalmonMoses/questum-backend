package com.theteam.questum.dto;

import com.theteam.questum.models.QuestParticipant;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class QuestParticipantDTO {
	@NonNull
	String name;

	@NonNull
	String email;

	@NonNull
	int points;

	@NonNull
	QuestGroupDTO group;

	public static QuestParticipantDTO of(QuestParticipant p) {
		return QuestParticipantDTO.builder()
		                          .name(p.getName())
		                          .email(p.getEmail())
		                          .group(QuestGroupDTO.of(p.getGroup()))
		                          .points(p.getPoints())
		                          .build();
	}
}
