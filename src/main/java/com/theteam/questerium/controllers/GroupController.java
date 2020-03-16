package com.theteam.questerium.controllers;

import com.google.api.client.util.IOUtils;
import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import com.theteam.questerium.dto.CompletedSubquestDTO;
import com.theteam.questerium.dto.QuestGroupDTO;
import com.theteam.questerium.dto.QuestGroupOwnerDTO;
import com.theteam.questerium.dto.QuestParticipantDTO;
import com.theteam.questerium.models.QuestGroup;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.requests.ChangeGroupRequest;
import com.theteam.questerium.requests.CreateGroupRequest;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.services.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups")
public class GroupController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final QuestParticipantRepository participants;
	@Autowired
	private final SecurityService security;
	@Autowired
	private MinioService minioService;

	GroupController(GroupRepository groups, GroupOwnerRepository owners,
	                QuestParticipantRepository participants, SecurityService securityService) {
		this.groups = groups;
		this.owners = owners;
		this.participants = participants;
		this.security = securityService;
	}

	@GetMapping
	public ResponseEntity<List<QuestGroupDTO>> all() {
		return ResponseEntity.ok(groups.findAll().stream().map(QuestGroupDTO::of)
		                               .collect(Collectors.toList()));
	}

	@PostMapping
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<QuestGroupDTO> createGroup(@RequestBody CreateGroupRequest req, Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		return owner.map(value -> {
			QuestGroup questGroup = new QuestGroup();
			questGroup.setName(req.getName());
			questGroup.setOwner(value);
			groups.save(questGroup);
			return new ResponseEntity<>(QuestGroupDTO.of(questGroup), HttpStatus.CREATED);
		}).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/{id}")
	public ResponseEntity<QuestGroupDTO> getById(@PathVariable Long id, Authentication auth) {
		Object principal = auth.getPrincipal();
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(principal, group.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return group
				.map(QuestGroupDTO::of)
				.map(ResponseEntity::ok)
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<QuestGroupDTO> changeById(@PathVariable Long id, @RequestBody ChangeGroupRequest req,
	                                                Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!group.get().getOwner().getEmail().equals(ownerEmail)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		group.get().setName(req.getName());
		groups.save(group.get());
		return group.map(QuestGroupDTO::of)
		            .map(ResponseEntity::ok)
		            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<?> deleteById(@PathVariable long id, Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!group.get().getOwner().getEmail().equals(ownerEmail)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		groups.delete(group.get());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/{id}/admin")
	public ResponseEntity<QuestGroupOwnerDTO> getOwnerOfGroup(@PathVariable Long id) {
		Optional<QuestGroup> group = groups.findById(id);
		return group.map(value -> QuestGroupOwnerDTO.of(value.getOwner()))
		            .map(ResponseEntity::ok)
		            .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@GetMapping("/{id}/leaderboard")
	public ResponseEntity<List<QuestParticipantDTO>> getGroupLeaderboard(@PathVariable long id, Authentication auth) {
		Object principal = auth.getPrincipal();
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(principal, group.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(participants.findAllByGroup_IdOrderByPointsDesc(id)
		                                     .stream()
		                                     .map(QuestParticipantDTO::of)
		                                     .collect(Collectors.toList()));
	}

	@GetMapping("/{id}/pending")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<List<CompletedSubquestDTO>> getPendingQuests(@PathVariable long id, Authentication auth) {
		Object principal = auth.getPrincipal();
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(principal, group.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(groups.findAllPendingSubquestsByGroup_Id(id)
		                               .stream()
		                               .map(CompletedSubquestDTO::of)
		                               .collect(Collectors.toList()));
	}

	@GetMapping("/{id}/avatar")
	public void getAvatar(@PathVariable long id, Authentication auth, HttpServletResponse res) throws IOException,
			MinioException {
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			res.setStatus(404);
			return;
		}
		if (!security.hasAccessToTheGroup(auth.getPrincipal(), group.get())) {
			res.setStatus(401);
			return;
		}
		String filename = "avatars/groups/" + String.valueOf(group.get().getId());

		InputStream inputStream = minioService.get(Path.of(filename));
		InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

		// Set the content type and attachment header.
		res.addHeader("Content-disposition", "attachment;filename=" + filename);
		res.setContentType(URLConnection.guessContentTypeFromStream(inputStream));

		// Copy the stream to the response's output stream.
		IOUtils.copy(inputStream, res.getOutputStream());
		res.flushBuffer();
	}
}
