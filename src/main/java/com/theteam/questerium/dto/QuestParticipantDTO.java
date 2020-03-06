package com.theteam.questerium.dto;

import com.theteam.questerium.models.QuestParticipant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuestParticipantDTO {
	long id;
	String name;
	String email;
	int points;
	long groupId;
	String profilePicLink;

	public static QuestParticipantDTO of(QuestParticipant p) {
		return QuestParticipantDTO.builder()
		                          .id(p.getId())
		                          .name(p.getName())
		                          .email(p.getEmail())
		                          .groupId(p.getGroup().getId())
		                          .points(p.getPoints())
		                          .profilePicLink("")
		                          .build();
	}
}
