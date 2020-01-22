package com.theteam.questum.controllers;

import com.theteam.questum.models.CompletedSubquest;
import com.theteam.questum.models.QuestGroup;
import com.theteam.questum.models.QuestGroupOwner;
import com.theteam.questum.repositories.CompletedSubquestsRepository;
import com.theteam.questum.repositories.GroupOwnerRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.requests.SubmitQuestAnswerRequest;
import com.theteam.questum.responses.SubmittedAnswerResponse;
import com.theteam.questum.security.GroupOwnerPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class QuestCompletionController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final CompletedSubquestsRepository quests;

	public QuestCompletionController(GroupRepository groups, GroupOwnerRepository owners,
	                                 CompletedSubquestsRepository quests) {
		this.groups = groups;
		this.owners = owners;
		this.quests = quests;
	}

	@PostMapping("/group/{group_id}/submit")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<SubmittedAnswerResponse> submitQuestAnswer(@PathVariable("id") long groupId,
	                                                                 @PathVariable("quest_id") long questId,
	                                                                 @RequestBody SubmitQuestAnswerRequest req,
	                                                                 Authentication auth) {
		return ResponseEntity.ok(null);
	}

	@PutMapping("group/{group_id}/verify/{verification_id}")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<SubmittedAnswerResponse> verifyQuestAnswer(@PathVariable("group_id") long groupId,
	                                                                 @PathVariable("verification_id") long verificationId,
	                                                                 @RequestBody SubmitQuestAnswerRequest req,
	                                                                 Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!group.get().getOwner().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Optional<CompletedSubquest> completedSub = quests.findById(verificationId);
		if (completedSub.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		CompletedSubquest subquest = completedSub.get();
		subquest.setVerified(true);
		SubmittedAnswerResponse res = SubmittedAnswerResponse.builder()
		                                                     .userId(subquest.getUser().getId())
		                                                     .subquestId(subquest.getSubquest().getId())
		                                                     .answer(subquest.getAnswer())
		                                                     .verified(true)
		                                                     .build();
		return ResponseEntity.ok(res);
	}
}
