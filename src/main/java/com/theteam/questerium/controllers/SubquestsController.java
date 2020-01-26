package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.SubquestDTO;
import com.theteam.questerium.models.Quest;
import com.theteam.questerium.models.QuestGroup;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.Subquest;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.repositories.QuestRepository;
import com.theteam.questerium.repositories.SubquestRepository;
import com.theteam.questerium.requests.AddSubquestRequest;
import com.theteam.questerium.requests.ChangeSubquestRequest;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.services.SecurityService;
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
@RequestMapping
public class SubquestsController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final QuestRepository quests;
	@Autowired
	private final SubquestRepository subquests;
	@Autowired
	private final SecurityService security;

	public SubquestsController(GroupRepository groups, GroupOwnerRepository owners, QuestRepository quests,
	                           SubquestRepository subquests, SecurityService security) {
		this.groups = groups;
		this.owners = owners;
		this.quests = quests;
		this.subquests = subquests;
		this.security = security;
	}

	@GetMapping("/groups/{group_id}/quests/{quest_id}/subquests")
	public ResponseEntity<List<SubquestDTO>> getAllSubquests(@PathVariable("group_id") long groupId, @PathVariable(
			"quest_id") long questId, Authentication auth) {
		Object principal = auth.getPrincipal();
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Optional<Quest> quest = quests.findByIdAndGroup_Id(questId, groupId);
		if (quest.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		// TODO: refactor with pattern matching in Java 14
		if (!security.hasAccessToTheGroup(principal, group.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(quest.get().getSubquests().stream().map(SubquestDTO::of).collect(Collectors.toList()));
	}

	@PostMapping("/groups/{group_id}/quests/{quest_id}/subquests")
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
		subquest.setDescription(req.getDesc());
		Long order = req.getOrder().orElse((long) quest.get().getSubquests().size());
		subquest.setOrder(order);
		subquest.setVerificationType(req.getVerification());
		subquest.setParentQuest(quest.get());
		quest.get().getSubquests().add(Math.toIntExact(order), subquest);
		subquests.save(subquest);
		return new ResponseEntity<SubquestDTO>(SubquestDTO.of(subquest), HttpStatus.CREATED);
	}

	@PutMapping("/{subquest_order}")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<SubquestDTO> changeSubquest(@PathVariable("group_id") long groupId,
	                                                  @PathVariable("quest_id") long questId,
	                                                  @PathVariable("subquest_order") long order,
	                                                  @RequestBody ChangeSubquestRequest req, Authentication auth) {
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
		Subquest subquest = quest.get().getSubquests().get((int) order);
		if (req.getDesc() != null) {
			subquest.setDescription(req.getDesc());
		}
		if (req.getVerificationType() != null) {
			subquest.setVerificationType(req.getVerificationType());
		}
		subquests.save(subquest);
		return new ResponseEntity<SubquestDTO>(SubquestDTO.of(subquest), HttpStatus.OK);
	}
}
