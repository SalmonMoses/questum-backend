package com.theteam.questum.controllers;

import com.theteam.questum.dto.QuestGroupOwnerDTO;
import com.theteam.questum.dto.QuestGroupDTO;
import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.models.QuestGroup;
import com.theteam.questum.repositories.GroupOwnersRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.requests.CreateGroupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups")
public class GroupsRestController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnersRepository owners;

	GroupsRestController(GroupRepository groups, GroupOwnersRepository owners) {
		this.groups = groups;
		this.owners = owners;
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
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/{id}/admin")
	public ResponseEntity<QuestGroupOwnerDTO> getOwnerOfGroup(@PathVariable Long id) {
		Optional<QuestGroup> group = groups.findById(id);
		return group.map(value -> QuestGroupOwnerDTO.of(value.getOwner()))
				.map(ResponseEntity::ok)
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PostMapping("/create")
	public ResponseEntity<QuestGroup> createGroup(@RequestBody CreateGroupRequest req) {
		Optional<GroupOwner> owner = owners.findById(req.getGroupOwnerId());
		if (owner.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return owner.map(value -> {
			QuestGroup questGroup = new QuestGroup();
			questGroup.setName(req.getName());
			questGroup.setOwner(value);
			groups.save(questGroup);
			return new ResponseEntity<>(questGroup, HttpStatus.CREATED);
		}).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}
