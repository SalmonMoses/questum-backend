package com.theteam.questum.dto;

import com.theteam.questum.models.Subquest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class SubquestDTO {
	@NonNull
	private String title;

	@NonNull
	private String description;

	@NonNull
	private String verificationType;

	@NonNull
	private Long order;

	public static SubquestDTO of(Subquest subquest) {
		return SubquestDTO.builder()
		                  .title(subquest.getTitle())
		                  .description(subquest.getDescription())
		                  .verificationType(subquest.getVerificationType())
		                  .order(subquest.getOrder())
		                  .build();
	}
}
