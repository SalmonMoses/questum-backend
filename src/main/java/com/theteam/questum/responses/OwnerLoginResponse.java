package com.theteam.questum.responses;

import com.theteam.questum.models.Group;
import com.theteam.questum.models.GroupOwner;
import lombok.Value;

import java.util.UUID;

@Value
public class OwnerLoginResponse {
	private final UUID token;
	private final GroupOwner owner;
}
