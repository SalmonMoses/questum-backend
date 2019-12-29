package com.theteam.questum.controllers;

import com.theteam.questum.dto.SubquestDTO;
import com.theteam.questum.models.Quest;
import com.theteam.questum.models.QuestGroup;
import com.theteam.questum.models.QuestGroupOwner;
import com.theteam.questum.models.Subquest;
import com.theteam.questum.repositories.GroupOwnerRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.repositories.QuestRepository;
import com.theteam.questum.repositories.SubquestRepository;
import com.theteam.questum.requests.AddSubquestRequest;
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
@RequestMapping("/groups/{group_id}/quests/{quest_id}/subquests")
public class SubquestsController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final QuestRepository quests;
	@Autowired
	private final SubquestRepository subquests;

	public SubquestsController(GroupRepository groups, GroupOwnerRepository owners, QuestRepository quests,
	                           SubquestRepository subquests) {
		this.groups = groups;
		this.owners = owners;
		this.quests = quests;
		this.subquests = subquests;
	}

	@GetMapping
	public ResponseEntity<List<SubquestDTO>> getAllSubquests(@PathVariable("group_id") long groupId, @PathVariable(
			"quest_id") long questId) {
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Optional<Quest> quest = quests.findByIdAndGroup_Id(questId, groupId);
		if (quest.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(quest.get().getSubquests().stream().map(SubquestDTO::of).collect(Collectors.toList()));
	}

	@PostMapping
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<SubquestDTO> addNewSubquest(@PathVariable("group_id") long groupId, @PathVariable(
			"quest_id") long questId, @RequestBody AddSubquestRequest req, Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!group.get().getOwner().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Optional<Quest> quest = quests.findByIdAndGroup_Id(questId, groupId);
		if (quest.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Subquest subquest = new Subquest();
		subquest.setTitle(req.getTitle());
		subquest.setDescription(req.getDesc());
		Long order = req.getOrder().orElse((long) quest.get().getSubquests().size());
		subquest.setOrder(order);
		subquest.setVerificationType(req.getVerification());
		subquest.setParentQuest(quest.get());
		quest.get().getSubquests().add(Math.toIntExact(order), subquest);
		subquests.save(subquest);
		return ResponseEntity.ok(SubquestDTO.of(subquest));
	}
}
