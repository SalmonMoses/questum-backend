package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.QuestDTO;
import com.theteam.questerium.models.Quest;
import com.theteam.questerium.models.QuestGroup;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.repositories.QuestRepository;
import com.theteam.questerium.requests.AddQuestRequest;
import com.theteam.questerium.requests.ChangeQuestRequest;
import com.theteam.questerium.security.GroupOwnerPrincipal;
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
@RequestMapping("/groups/{id}/quests")
public class QuestsController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final QuestRepository quests;

	public QuestsController(GroupRepository groups, GroupOwnerRepository owners,
	                        QuestRepository quests) {
		this.groups = groups;
		this.owners = owners;
		this.quests = quests;
	}

	@GetMapping
	public ResponseEntity<List<QuestDTO>> getGroupsQuests(@PathVariable long id) {
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(quests.findAllByGroup_Id(id).stream().map(QuestDTO::of).collect(Collectors.toList()));
	}

	@PostMapping
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
		return new ResponseEntity<QuestDTO>(QuestDTO.of(quest), HttpStatus.CREATED);
	}

	@GetMapping("/{quest_id}")
	public ResponseEntity<QuestDTO> getQuestById(@PathVariable("id") long groupId,
	                                             @PathVariable("quest_id") long questId, Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!group.get().getOwner().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Optional<Quest> questOpt = quests.findByIdAndGroup_Id(questId, groupId);
		if (questOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(QuestDTO.of(questOpt.get()));
	}

	@PutMapping("/{quest_id}")
	public ResponseEntity<QuestDTO> changeQuest(@PathVariable("id") long groupId,
	                                            @PathVariable("quest_id") long questId,
	                                            @RequestBody ChangeQuestRequest req, Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!group.get().getOwner().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Optional<Quest> questOpt = quests.findByIdAndGroup_Id(questId, groupId);
		if (questOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Quest quest = questOpt.get();
		if(req.getTitle() != null) quest.setTitle(req.getTitle());
		if(req.getDesc() != null) quest.setDesc(req.getDesc());
		quests.save(quest);
		return ResponseEntity.ok(QuestDTO.of(quest));
	}

	@DeleteMapping("/{quest_id}")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<?> deleteQuestById(@PathVariable("id") long groupId,
	                                             @PathVariable("quest_id") long questId, Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!group.get().getOwner().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Optional<Quest> questOpt = quests.findByIdAndGroup_Id(questId, groupId);
		if (questOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		quests.delete(questOpt.get());
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
