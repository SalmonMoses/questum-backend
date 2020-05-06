package com.theteam.questerium.responses;

import lombok.Builder;
import lombok.Value;
import org.codehaus.jackson.annotate.JsonProperty;

@Value
@Builder
public class QuestumError {
	String error;
	@JsonProperty("error_description") String desc;
}
