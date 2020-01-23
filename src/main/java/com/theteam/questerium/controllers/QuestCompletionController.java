package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.CompletedSubquestDTO;
import com.theteam.questerium.models.CompletedSubquest;
import com.theteam.questerium.models.QuestGroup;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.Subquest;
import com.theteam.questerium.repositories.*;
import com.theteam.questerium.requests.SubmitQuestAnswerRequest;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
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
	private final QuestParticipantRepository participants;
	@Autowired
	private final SubquestRepository subquests;
	@Autowired
	private final CompletedSubquestsRepository completedSubquests;

	public QuestCompletionController(GroupRepository groups, GroupOwnerRepository owners,
	                                 QuestParticipantRepository participants, SubquestRepository subquests,
	                                 CompletedSubquestsRepository completedSubquests) {
		this.groups = groups;
		this.owners = owners;
		this.participants = participants;
		this.subquests = subquests;
		this.completedSubquests = completedSubquests;
	}

	@PostMapping("/groups/{group_id}/submit")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<CompletedSubquestDTO> submitQuestAnswer(@PathVariable("group_id") long groupId,
	                                                              @RequestBody SubmitQuestAnswerRequest req,
	                                                              Authentication auth) {
		String userEmail = ((ParticipantPrincipal) auth.getPrincipal()).getEmail();
		if (groups.findById(groupId).isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Optional<Subquest> maybeSub = subquests.findById(req.getSubquestId());
		if (maybeSub.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (maybeSub.get().getParentQuest().getGroup().getId() != groupId) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		if (!maybeSub.get().getVerificationType().equals("NONE") && req.getAnswer().equals("")) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		CompletedSubquest completedSub = new CompletedSubquest();
		completedSub.setUser(participants.findByEmailAndGroup_Id(userEmail, groupId).get());
		completedSub.setSubquest(maybeSub.get());
		completedSub.setAnswer(req.getAnswer());
		completedSub.setVerified(maybeSub.get().getVerificationType().equals("NONE"));
		completedSubquests.save(completedSub);
		return ResponseEntity.ok(CompletedSubquestDTO.of(completedSub));
	}

	@PutMapping("groups/{group_id}/verify")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<CompletedSubquestDTO> verifyQuestAnswer(@PathVariable("group_id") long groupId,
	                                                              @RequestParam("verification_id") long verificationId,
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
		Optional<CompletedSubquest> completedSub = completedSubquests.findById(verificationId);
		if (completedSub.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		CompletedSubquest subquest = completedSub.get();
		subquest.setVerified(true);
		completedSubquests.save(subquest);
		CompletedSubquestDTO res = CompletedSubquestDTO.of(subquest);
		return ResponseEntity.ok(res);
	}

	@PutMapping("groups/{group_id}/reject")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<CompletedSubquestDTO> rejectQuestAnswer(@PathVariable("group_id") long groupId,
	                                                              @RequestParam("verification_id") long verificationId,
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
		Optional<CompletedSubquest> completedSub = completedSubquests.findById(verificationId);
		if (completedSub.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if(completedSub.get().isVerified()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		CompletedSubquest subquest = completedSub.get();
		completedSubquests.delete(subquest);
		return new ResponseEntity(HttpStatus.OK);
	}
}
