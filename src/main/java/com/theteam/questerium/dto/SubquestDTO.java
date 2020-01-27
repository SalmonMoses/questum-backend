package com.theteam.questerium.dto;

import com.theteam.questerium.models.Subquest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SubquestDTO {
	@NonNull
	private String description;

	@NonNull
	private String verificationType;

	@NonNull
	private Long order;

	public static SubquestDTO of(Subquest subquest) {
		return SubquestDTO.builder()
		                  .description(subquest.getDescription())
		                  .verificationType(subquest.getVerificationType())
		                  .order(subquest.getOrder())
		                  .build();
	}
}