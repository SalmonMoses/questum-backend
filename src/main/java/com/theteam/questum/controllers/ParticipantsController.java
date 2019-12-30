package com.theteam.questum.controllers;

import com.theteam.questum.dto.QuestParticipantDTO;
import com.theteam.questum.models.QuestGroup;
import com.theteam.questum.models.QuestParticipant;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.repositories.QuestParticipantRepository;
import com.theteam.questum.requests.AddParticipantRequest;
import com.theteam.questum.security.GroupOwnerPrincipal;
import com.theteam.questum.services.SHA512Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
public class ParticipantsController {
	@Autowired
	private QuestParticipantRepository participants;
	@Autowired
	private GroupRepository groups;
	@Autowired
	private SHA512Service encrypter;
	private final Random randomGen = new Random();

	public ParticipantsController(QuestParticipantRepository participants,
	                              GroupRepository groups, SHA512Service encrypter) {
		this.participants = participants;
		this.groups = groups;
		this.encrypter = encrypter;
	}

	@GetMapping("/participants")
	public ResponseEntity<List<QuestParticipantDTO>> getAllParticipants() {
		return ResponseEntity.ok(participants.findAll()
		                                     .stream()
		                                     .map(QuestParticipantDTO::of)
		                                     .collect(Collectors.toList()));
	}

	@GetMapping("/groups/{id}/participants")
	public ResponseEntity<List<QuestParticipantDTO>> getAllParticipantsInGroup(@PathVariable long id) {
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(participants.findAllByGroup_Id(id)
		                                     .stream()
		                                     .map(QuestParticipantDTO::of)
		                                     .collect(Collectors.toList()));
	}

	@PostMapping("/groups/{id}/participants")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<QuestParticipantDTO> addParticipant(@PathVariable long id,
	                                                          @RequestBody AddParticipantRequest req,
	                                                          Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (participants.existsByEmailAndGroup_Id(req.getEmail(), id)) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		if (!group.get().getOwner().getEmail().equals(ownerEmail)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		QuestParticipant participant = new QuestParticipant();
		participant.setEmail(req.getEmail());
		participant.setName(req.getName());
		participant.setGroup(group.get());
		participant.setPoints(0);
		int passwordNum = randomGen.nextInt(10000) + 1000;
		String passwordEncrypt = encrypter.saltAndEncrypt(req.getEmail(), Integer.toString(passwordNum));
		participant.setPassword(passwordEncrypt);
		participants.save(participant);
		return new ResponseEntity(QuestParticipantDTO.of(participant), HttpStatus.CREATED);
	}
}
