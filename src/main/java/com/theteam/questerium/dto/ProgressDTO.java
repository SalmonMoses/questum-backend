package com.theteam.questerium.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProgressDTO {
	long quest_id;
	double progress;
}
