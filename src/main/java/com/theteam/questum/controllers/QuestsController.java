package com.theteam.questum.controllers;

import com.theteam.questum.dto.QuestDTO;
import com.theteam.questum.models.Quest;
import com.theteam.questum.models.QuestGroup;
import com.theteam.questum.models.QuestGroupOwner;
import com.theteam.questum.repositories.GroupOwnerRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.repositories.QuestRepository;
import com.theteam.questum.requests.AddQuestRequest;
import com.theteam.questum.security.GroupOwnerPrincipal;
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
		return ResponseEntity.ok(QuestDTO.of(quest));
	}
}
