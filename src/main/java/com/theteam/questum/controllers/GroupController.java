package com.theteam.questum.controllers;

import com.theteam.questum.dto.QuestDTO;
import com.theteam.questum.dto.QuestGroupDTO;
import com.theteam.questum.dto.QuestGroupOwnerDTO;
import com.theteam.questum.dto.QuestParticipantDTO;
import com.theteam.questum.models.Quest;
import com.theteam.questum.models.QuestGroup;
import com.theteam.questum.models.QuestGroupOwner;
import com.theteam.questum.models.QuestParticipant;
import com.theteam.questum.repositories.GroupOwnerRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.repositories.QuestParticipantRepository;
import com.theteam.questum.repositories.QuestRepository;
import com.theteam.questum.requests.AddParticipantRequest;
import com.theteam.questum.requests.AddQuestRequest;
import com.theteam.questum.requests.ChangeGroupRequest;
import com.theteam.questum.requests.CreateGroupRequest;
import com.theteam.questum.security.GroupOwnerPrincipal;
import com.theteam.questum.services.SHA512Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups")
public class GroupController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final QuestParticipantRepository participants;
	@Autowired
	private final QuestRepository quests;
	@Autowired
	private final SHA512Service encrypter;
	private final Random randomGen = new Random();

	GroupController(GroupRepository groups, GroupOwnerRepository owners,
	                QuestParticipantRepository participants, QuestRepository quests, SHA512Service encrypter) {
		this.groups = groups;
		this.owners = owners;
		this.participants = participants;
		this.quests = quests;
		this.encrypter = encrypter;
	}

	@GetMapping
	public ResponseEntity<List<QuestGroupDTO>> all() {
		return ResponseEntity.ok(groups.findAll().stream().map(QuestGroupDTO::of)
		                               .collect(Collectors.toList()));
	}

	@PostMapping
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<QuestGroupDTO> createGroup(@RequestBody CreateGroupRequest req, Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		return owner.map(value -> {
			QuestGroup questGroup = new QuestGroup();
			questGroup.setName(req.getName());
			questGroup.setOwner(value);
			groups.save(questGroup);
			return new ResponseEntity<>(QuestGroupDTO.of(questGroup), HttpStatus.CREATED);
		}).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/{id}")
	public ResponseEntity<QuestGroupDTO> getById(@PathVariable Long id) {
		Optional<QuestGroup> group = groups.findById(id);
		return group
				.map(QuestGroupDTO::of)
				.map(ResponseEntity::ok)
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PutMapping("/{id}")
	public ResponseEntity<QuestGroupDTO> changeById(@PathVariable Long id, @RequestBody ChangeGroupRequest req,
	                                                Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!group.get().getOwner().getEmail().equals(ownerEmail)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		group.get().setName(req.getName());
		groups.save(group.get());
		return group.map(QuestGroupDTO::of)
		     .map(ResponseEntity::ok)
		     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/{id}/admin")
	public ResponseEntity<QuestGroupOwnerDTO> getOwnerOfGroup(@PathVariable Long id) {
		Optional<QuestGroup> group = groups.findById(id);
		return group.map(value -> QuestGroupOwnerDTO.of(value.getOwner()))
		            .map(ResponseEntity::ok)
		            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/{id}/participants")
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

	@PostMapping("/{id}/participants")
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

	@GetMapping("/{id}/quests")
	public ResponseEntity<List<QuestDTO>> getGroupsQuests(@PathVariable long id) {
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(quests.findAllByGroup_Id(id).stream().map(QuestDTO::of).collect(Collectors.toList()));
	}

	@PostMapping("/{id}/quests")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<QuestDTO> addQuest(@PathVariable long id, @RequestBody AddQuestRequest req,
	                                         Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!group.get().getOwner().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Quest quest = new Quest();
		quest.setTitle(req.getTitle());
		quest.setDesc(req.getDesc());
		quest.setGroup(group.get());
		quests.save(quest);
		return ResponseEntity.ok(QuestDTO.of(quest));
	}
}
