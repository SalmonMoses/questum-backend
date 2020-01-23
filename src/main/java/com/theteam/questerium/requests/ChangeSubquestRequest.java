package com.theteam.questerium.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeSubquestRequest {
	private String desc;
	private String verificationType;
}
