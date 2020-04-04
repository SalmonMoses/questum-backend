package com.theteam.questerium.dto;

import com.theteam.questerium.models.Subquest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SubquestDTO {
	@NonNull
	private long id;

	@NonNull
	private String desc;

	@NonNull
	private String verificationType;

	private String expectedAnswer;

	@NonNull
	private Long order;

	public static SubquestDTO of(Subquest subquest) {
		return SubquestDTO.builder()
		                  .id(subquest.getId())
		                  .desc(subquest.getDescription())
		                  .verificationType(subquest.getVerificationType())
		                  .expectedAnswer(subquest.getExpectedAnswer())
		                  .order(subquest.getOrder())
		                  .build();
	}
}
