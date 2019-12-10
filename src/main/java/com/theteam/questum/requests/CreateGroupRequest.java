package com.theteam.questum.requests;

import lombok.NonNull;
import lombok.Value;

@Value
public class CreateGroupRequest {
	@NonNull String name;
	long groupOwnerId;
}
