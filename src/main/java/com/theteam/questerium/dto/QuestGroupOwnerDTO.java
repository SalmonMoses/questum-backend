package com.theteam.questerium.dto;

import com.theteam.questerium.models.QuestGroupOwner;
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
