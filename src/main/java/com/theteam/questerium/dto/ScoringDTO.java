package com.theteam.questerium.dto;

import com.theteam.questerium.models.CompletedQuest;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScoringDTO {
	long questId;
	String title;
	long points;

	public static ScoringDTO of(CompletedQuest q) {
		return ScoringDTO.builder()
		                 .questId(q.getQuest().getId())
		                 .title(q.getQuest().getTitle())
		                 .points(q.getPoints())
		                 .build();
	}
}
