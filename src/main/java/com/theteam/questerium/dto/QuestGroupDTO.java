package com.theteam.questerium.dto;

import com.theteam.questerium.models.QuestGroup;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuestGroupDTO {
	long id;
	String name;
	QuestGroupOwnerDTO owner;
	long participantCount;

	public static QuestGroupDTO of(QuestGroup group) {
		return QuestGroupDTO.builder()
		                    .id(group.getId())
		                    .name(group.getName())
		                    .owner(QuestGroupOwnerDTO.of(group.getOwner()))
		                    .participantCount(group.getParticipants() != null ? group.getParticipants().size() : 0)
		                    .build();
	}
}
