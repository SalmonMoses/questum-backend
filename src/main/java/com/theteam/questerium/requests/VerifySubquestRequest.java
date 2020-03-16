package com.theteam.questerium.requests;

import lombok.Value;

@Value
public class VerifySubquestRequest {
	long subquestId;
	long userId;
}
