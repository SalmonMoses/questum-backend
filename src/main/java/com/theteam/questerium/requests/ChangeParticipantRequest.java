package com.theteam.questerium.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeParticipantRequest {
	private String name;
	private String email;
	private String password;
}
