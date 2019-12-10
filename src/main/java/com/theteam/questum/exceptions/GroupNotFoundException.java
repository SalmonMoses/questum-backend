package com.theteam.questum.exceptions;

public class GroupNotFoundException extends RuntimeException {
	public GroupNotFoundException(Long id) {
		super("Unknown group: " + id);
	}
}
