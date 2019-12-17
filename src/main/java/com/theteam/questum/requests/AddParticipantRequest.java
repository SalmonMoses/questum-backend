package com.theteam.questum.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class AddParticipantRequest {
	@NonNull
	String email;

	@NonNull
	String name;
}
