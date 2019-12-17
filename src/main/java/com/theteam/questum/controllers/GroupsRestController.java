package com.theteam.questum.controllers;

import com.theteam.questum.dto.QuestGroupDTO;
import com.theteam.questum.dto.QuestGroupOwnerDTO;
import com.theteam.questum.dto.QuestParticipantDTO;
import com.theteam.questum.exceptions.GroupNotFoundException;
import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.models.QuestGroup;
import com.theteam.questum.models.QuestParticipant;
import com.theteam.questum.repositories.GroupOwnersRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.repositories.QuestParticipantRepository;
import com.theteam.questum.requests.AddParticipantRequest;
import com.theteam.questum.requests.CreateGroupRequest;
import com.theteam.questum.security.GroupOwnerPrincipal;
import com.theteam.questum.services.SHA512Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups")
public class GroupsRestController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnersRepository owners;
	@Autowired
	private final QuestParticipantRepository participants;
	@Autowired
	private final SHA512Service encrypter;
	private final Random randomGen = new Random();

	GroupsRestController(GroupRepository groups, GroupOwnersRepository owners,
	                     QuestParticipantRepository participants, SHA512Service encrypter) {
		this.groups = groups;
		this.owners = owners;
		this.participants = participants;
		this.encrypter = encrypter;
	}

	@GetMapping("/all")
	@PreAuthorize("permitAll()")
	public ResponseEntity<List<QuestGroupDTO>> all() {
		return new ResponseEntity<List<QuestGroupDTO>>(groups.findAll().stream().map(QuestGroupDTO::of)
		                                                     .collect(Collectors.toList()), HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<QuestGroupDTO> getById(@PathVariable Long id) {
		Optional<QuestGroup> group = groups.findById(id);
		return group
				.map(QuestGroupDTO::of)
				.map(ResponseEntity::ok)
				.orElseThrow(() -> new GroupNotFoundException(id));
	}

	@GetMapping("/{id}/admin")
	public ResponseEntity<QuestGroupOwnerDTO> getOwnerOfGroup(@PathVariable Long id) {
		Optional<QuestGroup> group = groups.findById(id);
		return group.map(value -> QuestGroupOwnerDTO.of(value.getOwner()))
		            .map(ResponseEntity::ok)
		            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PostMapping("/create")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
	public ResponseEntity<QuestGroupDTO> createGroup(@RequestBody CreateGroupRequest req, Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<GroupOwner> owner = owners.findByEmail(ownerEmail);
		return owner.map(value -> {
			QuestGroup questGroup = new QuestGroup();
			questGroup.setName(req.getName());
			questGroup.setOwner(value);
			groups.save(questGroup);
			return new ResponseEntity<>(QuestGroupDTO.of(questGroup), HttpStatus.CREATED);
		}).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PostMapping("/{id}/addParticipant")
	@PreAuthorize("hasRole('ROLE_ADMIN')")
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
		String passwordDecrypt = Integer.toString(passwordNum) + participant.getEmail();
		String passwordEncrypt = encrypter.encrypt(passwordDecrypt);
		participant.setPassword(passwordEncrypt);
		participants.save(participant);
		return new ResponseEntity(QuestParticipantDTO.of(participant), HttpStatus.CREATED);
	}

	@GetMapping("/{id}/leaderboard")
	public ResponseEntity<List<QuestParticipantDTO>> getGroupLeaderboard(@PathVariable long id) {
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(participants.findAllByGroup_Id(id)
		                                     .stream()
		                                     .sorted(Comparator.comparingInt(o -> o.getPoints() * (-1)))
		                                     .map(QuestParticipantDTO::of)
		                                     .collect(Collectors.toList()));
	}
}
