package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.QuestDTO;
import com.theteam.questerium.models.Quest;
import com.theteam.questerium.models.QuestGroup;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.repositories.QuestRepository;
import com.theteam.questerium.requests.AddQuestRequest;
import com.theteam.questerium.requests.ChangeQuestRequest;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
import com.theteam.questerium.services.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class QuestsController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final QuestRepository quests;
	@Autowired
	private final QuestParticipantRepository participants;
	@Autowired
	private final SecurityService security;

	public QuestsController(GroupRepository groups, GroupOwnerRepository owners,
	                        QuestRepository quests, QuestParticipantRepository participants, SecurityService security) {
		this.groups = groups;
		this.owners = owners;
		this.quests = quests;
		this.participants = participants;
		this.security = security;
	}

	@GetMapping("/groups/{id}/quests")
	public ResponseEntity<List<QuestDTO>> getGroupsQuests(@PathVariable long id, Authentication auth) {
		Object principal = auth.getPrincipal();
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// TODO: refactor with pattern matching in Java 14
		if (!security.hasAccessToTheGroup(principal, group.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(quests.findAllByGroup_Id(id).stream().map(QuestDTO::of).collect(Collectors.toList()));
	}

	@PostMapping("/groups/{id}/quests")
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
		quest.setPoints(req.getPoints());
		quests.save(quest);
		log.info("Quest #{} was added to group #{} (owner #{})", quest.getId(), group.get()
		                                                                                         .getId(), group.get()
		                                                                                                        .getOwner()
		                                                                                                        .getId());
		return new ResponseEntity<QuestDTO>(QuestDTO.of(quest), HttpStatus.CREATED);
	}

	@GetMapping("/quests/{quest_id}")
	public ResponseEntity<QuestDTO> getQuestById(@PathVariable("quest_id") long questId, Authentication auth) {
		Optional<Quest> questOpt = quests.findById(questId);
		if (questOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Object principal = auth.getPrincipal();
		// TODO: refactor with pattern matching in Java 14
		if(principal instanceof GroupOwnerPrincipal) {
			String ownerEmail = ((GroupOwnerPrincipal) principal).getEmail();
			Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
			if(!owner.get().getQuestGroups().contains(questOpt.get().getGroup())) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
		} else if (principal instanceof ParticipantPrincipal) {
			String userEmail = ((ParticipantPrincipal) principal).getEmail();
			Optional<QuestParticipant> maybeUser = participants.findByEmailAndGroup_Id(userEmail, questOpt.get().getGroup().getId());
			if(maybeUser.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
		}
		return ResponseEntity.ok(QuestDTO.of(questOpt.get()));
	}

	@PutMapping("/quests/{quest_id}")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<QuestDTO> changeQuest(@PathVariable("quest_id") long questId,
	                                            @RequestBody ChangeQuestRequest req, Authentication auth) {
		Optional<Quest> questOpt = quests.findById(questId);
		if (questOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		if(!questOpt.get().getGroup().getOwner().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Quest quest = questOpt.get();
		if (req.getTitle() != null) {
			quest.setTitle(req.getTitle());
		}
		if (req.getDesc() != null) {
			quest.setDesc(req.getDesc());
		}
		if(req.getPoints() != null) {
			quest.setPoints(req.getPoints());
		}
		quests.save(quest);
		log.info("Quest #{} (group #{}) was updated", quest.getId(), quest.getGroup().getId());
		return ResponseEntity.ok(QuestDTO.of(quest));
	}

	@DeleteMapping("/quests/{quest_id}")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<?> deleteQuestById(@PathVariable("quest_id") long questId, Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<Quest> questOpt = quests.findById(questId);
		if (questOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		if (!questOpt.get().getGroup().getOwner().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		quests.delete(questOpt.get());
		log.info("Quest #{} (group #{}) was deleted", questOpt.get().getId(), questOpt.get().getGroup().getId());
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
