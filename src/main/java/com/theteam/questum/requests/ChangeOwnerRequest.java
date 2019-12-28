package com.theteam.questum.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeOwnerRequest {
	private String name;
	private String email;
	private String password;
}
