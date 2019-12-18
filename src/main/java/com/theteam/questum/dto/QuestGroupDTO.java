package com.theteam.questum.dto;

import com.theteam.questum.models.QuestGroup;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuestGroupDTO {
	long id;
	String name;
	QuestGroupOwnerDTO owner;

	public static QuestGroupDTO of(QuestGroup group) {
		return QuestGroupDTO.builder()
		                    .id(group.getId())
		                    .name(group.getName())
		                    .owner(QuestGroupOwnerDTO.of(group.getOwner()))
		                    .build();
	}
}
