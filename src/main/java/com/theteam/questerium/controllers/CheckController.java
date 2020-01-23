package com.theteam.questerium.controllers;

import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.responses.GroupCheckResponse;
import com.theteam.questerium.responses.ParticipantCheckResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/check")
public class CheckController {
	@Autowired
	private GroupRepository groups;

	@Autowired
	private QuestParticipantRepository participants;

	public CheckController(GroupRepository groups, QuestParticipantRepository participants) {
		this.groups = groups;
		this.participants = participants;
	}

	@GetMapping("/group")
	public ResponseEntity<GroupCheckResponse> checkGroup(@RequestParam long id) {
		boolean exists = groups.existsById(id);
		GroupCheckResponse res = GroupCheckResponse.builder().type("group").id(id).exists(exists).build();
		return ResponseEntity.ok(res);
	}

	@GetMapping("/participant")
	public ResponseEntity<ParticipantCheckResponse> checkParticipant(@RequestParam long groupId,
	                                                                 @RequestParam String email) {
		boolean exists = groups.existsById(groupId);
		ParticipantCheckResponse res = ParticipantCheckResponse.builder()
		                                                 .type("participant")
		                                                 .groupId(groupId)
		                                                 .email(email)
		                                                 .exists(exists)
		                                                 .build();
		return ResponseEntity.ok(res);
	}
}
