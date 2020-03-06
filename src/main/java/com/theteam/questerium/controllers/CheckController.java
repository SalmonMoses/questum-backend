package com.theteam.questerium.controllers;

import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.requests.CheckPasswordRequest;
import com.theteam.questerium.responses.GroupCheckResponse;
import com.theteam.questerium.responses.ParticipantCheckResponse;
import com.theteam.questerium.responses.PasswordCheckResponse;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
import com.theteam.questerium.services.SHA512Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/check")
public class CheckController {
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final SHA512Service encrypter;
	@Autowired
	private GroupRepository groups;
	@Autowired
	private QuestParticipantRepository participants;

	public CheckController(GroupRepository groups, QuestParticipantRepository participants,
	                       GroupOwnerRepository owners, SHA512Service encryptor) {
		this.groups = groups;
		this.participants = participants;
		this.owners = owners;
		this.encrypter = encryptor;
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

	@GetMapping("/password")
	public ResponseEntity<PasswordCheckResponse> checkPassword(@RequestBody CheckPasswordRequest req,
	                                                           Authentication auth) {
		Object principal = auth.getPrincipal();
		if (req.getType() == 0 && principal instanceof GroupOwnerPrincipal) {
			Optional<QuestGroupOwner> maybeOwner = owners.findById(req.getId());
			if (maybeOwner.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			GroupOwnerPrincipal ownerPrincipal = (GroupOwnerPrincipal) principal;
			if (!ownerPrincipal.getEmail().equals(maybeOwner.get().getEmail())) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			String expectedPassword = encrypter.saltAndEncrypt(ownerPrincipal.getEmail(), req.getPassword());
			PasswordCheckResponse res = PasswordCheckResponse.builder()
			                                                 .type(req.getType())
			                                                 .id(req.getId())
			                                                 .correct(expectedPassword.equalsIgnoreCase(maybeOwner.get().getPassword()))
			                                                 .build();
			return ResponseEntity.ok(res);
		}
		else if (req.getType() == 1 && principal instanceof ParticipantPrincipal) {
			Optional<QuestParticipant> maybeParticipant = participants.findById(req.getId());
			if (maybeParticipant.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			ParticipantPrincipal participantPrincipal = (ParticipantPrincipal) principal;
			if (!participantPrincipal.getEmail().equals(maybeParticipant.get().getEmail())) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			String expectedPassword = encrypter.saltAndEncrypt(participantPrincipal.getEmail(), req.getPassword());
			PasswordCheckResponse res = PasswordCheckResponse.builder()
			                                                 .type(req.getType())
			                                                 .id(req.getId())
			                                                 .correct(expectedPassword.equalsIgnoreCase(maybeParticipant.get().getPassword()))
			                                                 .build();
			return ResponseEntity.ok(res);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}
}
