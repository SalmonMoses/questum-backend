package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.SubquestDTO;
import com.theteam.questerium.models.Quest;
import com.theteam.questerium.models.Subquest;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.repositories.QuestRepository;
import com.theteam.questerium.repositories.SubquestRepository;
import com.theteam.questerium.requests.AddSubquestRequest;
import com.theteam.questerium.requests.ChangeSubquestRequest;
import com.theteam.questerium.services.QuestService;
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
	@Autowired
	private QuestService questService;

	public SubquestsController(GroupRepository groups, GroupOwnerRepository owners, QuestRepository quests,
	                           SubquestRepository subquests, SecurityService security) {
		this.groups = groups;
		this.owners = owners;
		this.quests = quests;
		this.subquests = subquests;
		this.security = security;
	}

	@GetMapping("/quests/{quest_id}/subquests")
	public ResponseEntity<List<SubquestDTO>> getAllSubquests(@PathVariable("quest_id") long questId,
	                                                         Authentication auth) {
		Object principal = auth.getPrincipal();
		Optional<Quest> quest = quests.findById(questId);
		if (quest.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(principal, quest.get().getGroup())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(quest.get()
		                              .getSubquests()
		                              .stream()
		                              .map(SubquestDTO::of)
		                              .collect(Collectors.toList()));
	}

	@PostMapping("/quests/{quest_id}/subquests")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<SubquestDTO> addNewSubquest(@PathVariable("quest_id") long questId,
	                                                  @RequestBody AddSubquestRequest req, Authentication auth) {
		Object principal = auth.getPrincipal();
		Optional<Quest> quest = quests.findById(questId);
		if (quest.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(principal, quest.get().getGroup())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Subquest subquest = new Subquest();
		subquest.setDescription(req.getDesc());
		long order = quest.get().getSubquests().size();
		subquest.setOrder(order);
		subquest.setVerificationType(req.getVerification());
		subquest.setParentQuest(quest.get());
		quest.get().getSubquests().add(subquest);
		subquests.save(subquest);
		return new ResponseEntity<>(SubquestDTO.of(subquest), HttpStatus.CREATED);
	}

	@PutMapping("/subquests/{id}")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<SubquestDTO> changeSubquest(@PathVariable long id, @RequestBody ChangeSubquestRequest req,
	                                                  Authentication auth) {
		Object principal = auth.getPrincipal();
		Optional<Subquest> subquestOpt = subquests.findById(id);
		if (subquestOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(principal, subquestOpt.get().getParentQuest().getGroup())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Subquest subquest = subquestOpt.get();
		if (req.getDesc() != null) {
			subquest.setDescription(req.getDesc());
		}
		if (req.getVerificationType() != null) {
			subquest.setVerificationType(req.getVerificationType());
		}
		subquests.save(subquest);
		return new ResponseEntity<SubquestDTO>(SubquestDTO.of(subquest), HttpStatus.OK);
	}

	@DeleteMapping("/subquests/{id}")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<SubquestDTO> deleteSubquest(@PathVariable long id,
	                                                  Authentication auth) {
		Object principal = auth.getPrincipal();
		Optional<Subquest> subquestOpt = subquests.findById(id);
		if (subquestOpt.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(principal, subquestOpt.get().getParentQuest().getGroup())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		Subquest subquest = subquestOpt.get();
		subquests.delete(subquest);
		subquests.findSubquestsByParentQuest_IdAndOrderGreaterThan(subquest.getParentQuest()
		                                                                   .getId(), subquest.getOrder())
		         .forEach(sub -> {
			         sub.setOrder(sub.getOrder() - 1);
			         subquests.save(sub);
		         });
		subquest.getParentQuest()
		        .getGroup()
		        .getParticipants()
		        .forEach(p -> questService.tryCompleteQuest(p, subquest.getParentQuest()));
		return new ResponseEntity<SubquestDTO>(HttpStatus.OK);
	}
}
