package com.theteam.questum.controllers;

import com.theteam.questum.dto.QuestGroupDTO;
import com.theteam.questum.dto.QuestGroupOwnerDTO;
import com.theteam.questum.models.QuestGroupOwner;
import com.theteam.questum.repositories.GroupOwnerRepository;
import com.theteam.questum.repositories.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/owners")
public class GroupOwnerController {
	@Autowired
	private GroupOwnerRepository owners;
	@Autowired
	private GroupRepository groups;

	@GetMapping
	public ResponseEntity<List<QuestGroupOwnerDTO>> getAll() {
		return ResponseEntity.ok(owners.findAll().stream().map(QuestGroupOwnerDTO::of).collect(Collectors.toList()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<QuestGroupOwnerDTO> getById(@PathVariable long id) {
		Optional<QuestGroupOwner> owner = owners.findById(id);
		return owner
				.map(QuestGroupOwnerDTO::of)
				.map(ResponseEntity::ok)
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/{id}/groups")
	public ResponseEntity<List<QuestGroupDTO>> getGroupsById(@PathVariable long id) {
		Optional<QuestGroupOwner> owner = owners.findById(id);
		if(owner.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		return ResponseEntity.ok(groups.findAllByOwner_Id(id).stream()
				.map(QuestGroupDTO::of).collect(Collectors.toList()));
	}
}
