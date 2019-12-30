package com.theteam.questum.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeQuestRequest {
	private String title;
	private String desc;
}
