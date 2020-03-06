package com.theteam.questerium.requests;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class CheckPasswordRequest {
	long type;
	long id;
	@NonNull
	String password;
}
