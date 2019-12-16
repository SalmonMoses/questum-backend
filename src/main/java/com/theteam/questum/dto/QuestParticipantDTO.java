package com.theteam.questum.dto;

import lombok.NonNull;
import lombok.Value;

@Value
public class QuestParticipantDTO {
	@NonNull
	String name;

	@NonNull
	int points;

	@NonNull
	QuestGroupDTO group;
}
