package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.QuestGroupDTO;
import com.theteam.questerium.dto.QuestGroupOwnerDTO;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.requests.ChangeOwnerRequest;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.services.SHA512Service;
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
@RequestMapping("/owners")
public class GroupOwnerController {
	@Autowired
	private GroupOwnerRepository owners;
	@Autowired
	private GroupRepository groups;
	@Autowired
	private SHA512Service encryptor;

	@GetMapping
	public ResponseEntity<List<QuestGroupOwnerDTO>> getAll() {
		return ResponseEntity.ok(owners.findAll().stream().map(QuestGroupOwnerDTO::of).collect(Collectors.toList()));
	}

	@GetMapping("/{id}")
	public ResponseEntity<QuestGroupOwnerDTO> getById(@PathVariable long id, Authentication auth) {
		Optional<QuestGroupOwner> owner = owners.findById(id);
		return owner
				.map(QuestGroupOwnerDTO::of)
				.map(ResponseEntity::ok)
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PutMapping("/{id}")
	public ResponseEntity<QuestGroupOwnerDTO> changeById(@PathVariable long id, @RequestBody ChangeOwnerRequest req,
	                                                     Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> user = owners.findByEmail(ownerEmail);
		Optional<QuestGroupOwner> owner = owners.findById(id);
		if (owner.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!user.get().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		QuestGroupOwner ownerObj = owner.get();
		if (req.getEmail() != null) {
			if (owners.existsByEmail(req.getEmail())) {
				return new ResponseEntity<>(HttpStatus.CONFLICT);
			}
			ownerObj.setEmail(req.getEmail());
			if (req.getPassword() == null) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}
		if (req.getName() != null) {
			ownerObj.setName(req.getName());
		}
		if (req.getPassword() != null) {
			String newPassword = encryptor.saltAndEncrypt(ownerObj.getEmail(), req.getPassword());
			ownerObj.setPassword(newPassword);
		}
		owners.save(ownerObj);
		return owner
				.map(QuestGroupOwnerDTO::of)
				.map(ResponseEntity::ok)
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<?> deleteById(@PathVariable long id,
	                                    Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> user = owners.findByEmail(ownerEmail);
		Optional<QuestGroupOwner> owner = owners.findById(id);
		if (owner.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!user.get().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		QuestGroupOwner ownerObj = owner.get();
		owners.delete(ownerObj);
		return new ResponseEntity(HttpStatus.OK);
	}

	@GetMapping("/{id}/groups")
	public ResponseEntity<List<QuestGroupDTO>> getGroupsById(@PathVariable long id) {
		Optional<QuestGroupOwner> owner = owners.findById(id);
		if (owner.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(groups.findAllByOwner_Id(id).stream()
		                               .map(QuestGroupDTO::of).collect(Collectors.toList()));
	}
}
