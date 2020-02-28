package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.QuestParticipantDTO;
import com.theteam.questerium.dto.ScoringDTO;
import com.theteam.questerium.models.Quest;
import com.theteam.questerium.models.QuestGroup;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.repositories.CompletedQuestsRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.repositories.QuestRepository;
import com.theteam.questerium.requests.AddParticipantRequest;
import com.theteam.questerium.requests.ChangeOwnerRequest;
import com.theteam.questerium.responses.ScoreResponse;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
import com.theteam.questerium.services.SHA512Service;
import com.theteam.questerium.services.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
public class ParticipantsController {
	@Autowired
	private final QuestParticipantRepository participants;
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final QuestRepository quests;
	@Autowired
	private final SHA512Service encrypter;
	@Autowired
	private final SecurityService security;
	@Autowired
	private final SHA512Service encryptor;
	@Autowired
	private final CompletedQuestsRepository completedQuests;

	private final Random randomGen = new Random();

	public ParticipantsController(QuestParticipantRepository participants,
	                              GroupRepository groups, QuestRepository quests, SHA512Service encrypter,
	                              SecurityService security,
	                              SHA512Service encryptor, CompletedQuestsRepository completedQuests) {
		this.participants = participants;
		this.groups = groups;
		this.quests = quests;
		this.encrypter = encrypter;
		this.security = security;
		this.encryptor = encryptor;
		this.completedQuests = completedQuests;
	}

	@GetMapping("/groups/{id}/participants")
	public ResponseEntity<List<QuestParticipantDTO>> getAllParticipantsInGroup(@PathVariable long id) {
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return ResponseEntity.ok(participants.findAllByGroup_Id(id)
		                                     .stream()
		                                     .map(QuestParticipantDTO::of)
		                                     .collect(Collectors.toList()));
	}

	@PostMapping("/groups/{id}/participants")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<QuestParticipantDTO> addParticipant(@PathVariable long id,
	                                                          @RequestBody AddParticipantRequest req,
	                                                          Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroup> group = groups.findById(id);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (participants.existsByEmailAndGroup_Id(req.getEmail(), id)) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		if (!group.get().getOwner().getEmail().equals(ownerEmail)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		QuestParticipant participant = new QuestParticipant();
		participant.setEmail(req.getEmail());
		participant.setName(req.getName());
		participant.setGroup(group.get());
		participant.setPoints(0);
		int passwordNum = randomGen.nextInt(10000) + 1000;
		String passwordEncrypt = encrypter.saltAndEncrypt(req.getEmail(), Integer.toString(passwordNum));
		participant.setPassword(passwordEncrypt);
		participants.save(participant);
		return new ResponseEntity(QuestParticipantDTO.of(participant), HttpStatus.CREATED);
	}

	@GetMapping("/participants/{id}")
	public ResponseEntity<QuestParticipantDTO> getParticipantById(@PathVariable long id,
	                                                              Authentication auth) {
		Optional<QuestParticipant> participant = participants.findById(id);
		if (participant.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Object principal = auth.getPrincipal();
		if (!security.hasAccessToTheGroup(principal, participant.get().getGroup())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return participant.map(QuestParticipantDTO::of).map(ResponseEntity::ok).get();
	}

	@PutMapping("/participants/{id}")
	@PreAuthorize("hasRole('ROLE_PARTICIPANT')")
	public ResponseEntity<QuestParticipantDTO> changeById(@PathVariable long id, @RequestBody ChangeOwnerRequest req,
	                                                      Authentication auth) {
		ParticipantPrincipal principal = (ParticipantPrincipal) auth.getPrincipal();
		Optional<QuestParticipant> participant = participants.findById(id);
		if (participant.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		QuestParticipant participantObj = participant.get();
		if (principal.getId() != id) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		if (req.getEmail() != null) {
			if (participants.existsByEmailAndGroup_Id(req.getEmail(), participantObj.getGroup().getId())) {
				return new ResponseEntity<>(HttpStatus.CONFLICT);
			}
			participantObj.setEmail(req.getEmail());
			if (req.getPassword() == null) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}
		if (req.getName() != null) {
			participantObj.setName(req.getName());
		}
		if (req.getPassword() != null) {
			String newPassword = encryptor.saltAndEncrypt(participantObj.getEmail(), req.getPassword());
			participantObj.setPassword(newPassword);
		}
		participants.save(participantObj);
		return participant
				.map(QuestParticipantDTO::of)
				.map(ResponseEntity::ok)
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
	}

	@DeleteMapping("/participants/{id}")
	public ResponseEntity<?> deleteById(@PathVariable long id,
	                                    Authentication auth) {
		Object principal = auth.getPrincipal();
		Optional<QuestParticipant> participant = participants.findById(id);
		if (participant.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheParticipant(principal, participant.get())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		participants.deleteById(id);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/participants/{id}/score")
	public ResponseEntity<ScoreResponse> getParticipantScoreById(@PathVariable long id,
	                                                             Authentication auth) {
		Optional<QuestParticipant> participant = participants.findById(id);
		if (participant.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(auth.getPrincipal(), participant.get().getGroup())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		long participantPoints = completedQuests.getParticipantScoreById(id);
		List<ScoringDTO> scorings = completedQuests.findAllByUser_Id(id)
		                                           .stream()
		                                           .map(ScoringDTO::of)
		                                           .collect(Collectors.toList());
		return ResponseEntity.ok(new ScoreResponse(participantPoints, scorings));
	}

	@GetMapping("/participants/{id}/progress")
	public ResponseEntity<Long> getProgressForQuest(@PathVariable long id, Authentication auth) {
		Optional<QuestParticipant> participant = participants.findById(id);
		if (participant.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(auth.getPrincipal(), participant.get().getGroup())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(participants.getProgressForQuest(id, 10));
	}

	@GetMapping("/participants/{id}/progress/{questId}")
	public ResponseEntity<Long> getUnProgressForQuest(@PathVariable long id, @PathVariable long questId,
	                                                  Authentication auth) {
		Optional<QuestParticipant> participant = participants.findById(id);
		Optional<Quest> quest = quests.findById(questId);
		if (participant.isEmpty() || quest.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(auth.getPrincipal(), participant.get()
		                                                                  .getGroup()) || !security.hasAccessToTheGroup(auth.getPrincipal(), quest
				.get()
				.getGroup())) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		return ResponseEntity.ok(participants.getRemainingSubquestsForQuestId(id, questId));
	}
}
