package com.theteam.questum.dto;

import com.theteam.questum.models.QuestGroup;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuestGroupDTO {
	String name;
	String ownerEmail;

	public static QuestGroupDTO of(QuestGroup group) {
		return QuestGroupDTO.builder().name(group.getName()).ownerEmail(group.getOwner().getEmail()).build();
	}
}
