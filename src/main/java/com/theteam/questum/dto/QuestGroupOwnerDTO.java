package com.theteam.questum.dto;

import com.theteam.questum.models.GroupOwner;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class QuestGroupOwnerDTO {
	String name;
	String email;

	public static QuestGroupOwnerDTO of(@NonNull GroupOwner owner) {
		return QuestGroupOwnerDTO.builder().email(owner.getEmail()).name(owner.getName())
		                         .build();
	}
}
