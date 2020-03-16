package com.theteam.questerium.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class AddSubquestRequest {
	@NonNull
	private String title;

	@NonNull
	private String desc;

	@NonNull
	private String verification;

	private String expectedAnswer;
}
