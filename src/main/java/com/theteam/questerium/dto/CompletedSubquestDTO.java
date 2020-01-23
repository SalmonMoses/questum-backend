package com.theteam.questerium.dto;

import com.theteam.questerium.models.CompletedSubquest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class CompletedSubquestDTO {
	long userId;
	long subquestId;
	@NonNull String answer;
	boolean verified;

	public static CompletedSubquestDTO of(CompletedSubquest subquest) {
		return CompletedSubquestDTO.builder()
		                           .userId(subquest.getUser().getId())
		                           .subquestId(subquest.getSubquest().getId())
		                           .answer(subquest.getAnswer())
		                           .verified(subquest.isVerified())
		                           .build();
	}
}
