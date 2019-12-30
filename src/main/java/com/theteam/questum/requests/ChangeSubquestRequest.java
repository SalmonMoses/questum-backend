package com.theteam.questum.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeSubquestRequest {
	private String title;
	private String desc;
	private String verificationType;
}
