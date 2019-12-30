package com.theteam.questum.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Optional;

@Data
@NoArgsConstructor
public class AddSubquestRequest {
	@NonNull
	private String title;

	@NonNull
	private String desc;

	@NonNull
	private String verification;

	private Optional<Long> order;
}
