package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.CompletedSubquestDTO;
import com.theteam.questerium.models.*;
import com.theteam.questerium.repositories.*;
import com.theteam.questerium.requests.SubmitQuestAnswerRequest;
import com.theteam.questerium.requests.VerifySubquestRequest;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
import com.theteam.questerium.services.QuestService;
import lombok.NonNull;
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
	@Autowired
	private CompletedQuestRepository completedQuests;
	@Autowired
	private QuestService questService;

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
	@PreAuthorize("hasRole('ROLE_PARTICIPANT')")
	public ResponseEntity<CompletedSubquestDTO> submitQuestAnswer(@PathVariable("group_id") long groupId,
	                                                              @RequestBody SubmitQuestAnswerRequest req,
	                                                              Authentication auth) {
		ParticipantPrincipal userPrincipal = (ParticipantPrincipal) auth.getPrincipal();
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
		CompletedSubquest completedSub = new CompletedSubquest();
		Optional<QuestParticipant> participant = participants.findById(userPrincipal.getId());
		if (maybeSub.get().getVerificationType().equalsIgnoreCase("NONE")) {
			if (!req.getAnswer().equals("")) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}
		completedSub.setUser(participant.get());
		completedSub.setSubquest(maybeSub.get());
		completedSub.setAnswer(req.getAnswer());
		completedSub.setVerified(false);
		if (maybeSub.get().getVerificationType().equalsIgnoreCase("NONE")) {
			completedSub.setVerified(true);
			completedSubquests.save(completedSub);
			questService.tryCompleteQuest(participant.get(), maybeSub.get().getParentQuest());
		} else if (maybeSub.get().getVerificationType().equalsIgnoreCase("TEXT") && req.getAnswer().equals(maybeSub.get().getExpectedAnswer())) {
			completedSub.setVerified(true);
			completedSubquests.save(completedSub);
			questService.tryCompleteQuest(participant.get(), maybeSub.get().getParentQuest());
		} else {
			completedSubquests.save(completedSub);
		}
		return ResponseEntity.ok(CompletedSubquestDTO.of(completedSub));
	}

	@PutMapping("groups/{group_id}/verify")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<CompletedSubquestDTO> verifyQuestAnswer(@PathVariable("group_id") long groupId,
	                                                              @RequestBody VerifySubquestRequest req,
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
		Optional<CompletedSubquest> completedSub = completedSubquests.findByUser_IdAndSubquest_Id(req.getUserId(),
		                                                                                          req.getSubquestId());
		if (completedSub.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		CompletedSubquest subquest = completedSub.get();
		if (req.isVerified()) {
			subquest.setVerified(true);
			completedSubquests.save(subquest);
			CompletedSubquestDTO res = CompletedSubquestDTO.of(subquest);
			@NonNull QuestParticipant user = subquest.getUser();
			questService.tryCompleteQuest(user, subquest.getSubquest().getParentQuest());
			return ResponseEntity.ok(res);
		} else {
			if (subquest.isVerified()) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			completedSubquests.delete(subquest);
			return new ResponseEntity<>(HttpStatus.OK);
		}
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
		if (completedSub.get().isVerified()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		CompletedSubquest subquest = completedSub.get();
		completedSubquests.delete(subquest);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
