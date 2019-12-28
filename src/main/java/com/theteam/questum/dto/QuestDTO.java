package com.theteam.questum.dto;

import com.theteam.questum.models.Quest;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuestDTO {
	long id;
	String title;
	String desc;

	public static QuestDTO of(Quest q) {
		return QuestDTO.builder().id(q.getId()).title(q.getTitle()).desc(q.getDesc()).build();
	}
}
