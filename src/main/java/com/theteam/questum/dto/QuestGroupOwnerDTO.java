package com.theteam.questum.dto;

import com.theteam.questum.models.QuestGroupOwner;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class QuestGroupOwnerDTO {
	long id;
	String name;
	String email;

	public static QuestGroupOwnerDTO of(@NonNull QuestGroupOwner owner) {
		return QuestGroupOwnerDTO.builder().id(owner.getId()).email(owner.getEmail()).name(owner.getName())
		                         .build();
	}
}
