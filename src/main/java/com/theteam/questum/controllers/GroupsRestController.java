package com.theteam.questum.controllers;

import com.theteam.questum.exceptions.GroupNotFoundException;
import com.theteam.questum.models.Group;
import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.repositories.GroupOwnersRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.requests.CreateGroupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/travels")
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
	public ResponseEntity<List<Group>> all() {
		return new ResponseEntity<>(groups.findAll(), HttpStatus.OK);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Group> getById(@PathVariable Long id) {
		Optional<Group> group = groups.findById(id);
		return group
				.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/{id}/admin")
	public ResponseEntity<GroupOwner> getOwnerOfGroup(@PathVariable Long id) {
		Optional<Group> group = groups.findById(id);
		return group
				.map(value -> new ResponseEntity<>(value.getOwner(), HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PostMapping("/create")
	public ResponseEntity<Group> createGroup(@RequestBody CreateGroupRequest req) {
		Optional<GroupOwner> owner = owners.findById(req.getGroupOwnerId());
		if (owner.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		return owner.map(value -> {
			Group group = new Group();
			group.setName(req.getName());
			group.setOwner(value);
			groups.save(group);
			return new ResponseEntity<>(group, HttpStatus.CREATED);
		}).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}
}
