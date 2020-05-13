package com.theteam.questerium.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeQuestRequest {
	private String title;
	private String desc;
	private Integer points;
}
