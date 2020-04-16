package com.theteam.questerium.dto;

import com.theteam.questerium.models.Subquest;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
public class ProgressDTO {
	double progress;
	List<SubquestDTO> subquests;

	public static ProgressDTO of(List<Subquest> subquests, double progress) {
		return ProgressDTO.builder()
		                  .subquests(subquests.stream()
		                                      .map(SubquestDTO::of)
		                                      .collect(Collectors.toList()))
		                  .progress(progress)
		                  .build();
	}
}
