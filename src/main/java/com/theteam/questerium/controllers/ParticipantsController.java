package com.theteam.questerium.controllers;

import com.google.api.client.util.IOUtils;
import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import com.theteam.questerium.dto.ProgressDTO;
import com.theteam.questerium.dto.QuestParticipantDTO;
import com.theteam.questerium.dto.ScoringDTO;
import com.theteam.questerium.models.*;
import com.theteam.questerium.repositories.*;
import com.theteam.questerium.requests.AddParticipantRequest;
import com.theteam.questerium.requests.ChangeOwnerRequest;
import com.theteam.questerium.responses.ScoreResponse;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
import com.theteam.questerium.services.EmailService;
import com.theteam.questerium.services.SHA512Service;
import com.theteam.questerium.services.SecurityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@Slf4j
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
	private final CompletedQuestRepository completedQuests;
	@Autowired
	private final CompletedSubquestsRepository completedSubquests;
	private final Random randomGen = new Random();
	@Autowired
	private MinioService minioService;
	@Autowired
	private EmailService emailService;

	public ParticipantsController(QuestParticipantRepository participants,
	                              GroupRepository groups, QuestRepository quests, SHA512Service encrypter,
	                              SecurityService security,
	                              SHA512Service encryptor, CompletedQuestRepository completedQuests,
	                              CompletedSubquestsRepository completedSubquests) {
		this.participants = participants;
		this.groups = groups;
		this.quests = quests;
		this.encrypter = encrypter;
		this.security = security;
		this.completedQuests = completedQuests;
		this.completedSubquests = completedSubquests;
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
		String password = Integer.toString(passwordNum);
		String passwordEncrypt = encrypter.saltAndEncrypt(req.getEmail(), password);
		participant.setPassword(passwordEncrypt);
		participants.save(participant);
		log.info("Participant #{} was added to group #{} (owner #{})", participant.getId(), group.get()
		                                                                                         .getId(), group.get()
		                                                                                                        .getOwner()
		                                                                                                        .getId());
		try {
			emailService.sendParticipantSignUpEmail(participant, password);
		} catch (IOException e) {
			log.error("Couldn't send sign up email to participant #{} (group: #{})", participant.getId(), group.get()
			                                                                                                   .getId());
			e.printStackTrace();
		}
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
			String newPassword = encrypter.saltAndEncrypt(participantObj.getEmail(), req.getPassword());
			participantObj.setPassword(newPassword);
		}
		participants.save(participantObj);
		log.info("Participant #{} (group: #{}) changed his info", participantObj.getId(), participantObj.getGroup()
		                                                                                                .getId());
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
		log.info("Participant #{} (group: #{}) was deleted", id, participant.get().getGroup().getId());
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

	@GetMapping("/participants/{id}/progress/{questId}")
	public ResponseEntity<ProgressDTO> getProgressForQuest(@PathVariable long id, @PathVariable long questId,
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
		double subProgress = participants.getProgressForQuest(id, questId);
		double percentage = quest.get().getSubquests().size() > 0
				? subProgress / quest.get().getSubquests().size()
				: 0;
		List<CompletedSubquest> completedSubquestsByUser = completedSubquests.findByUser_IdAndQuest_Id(id, questId);
		if (completedSubquestsByUser.size() != 0) {
			if (!completedSubquestsByUser.get(completedSubquestsByUser.size() - 1).isVerified()) {
				subProgress += 0.5;
			}
		}
		List<Subquest> subquests = quest.get().getSubquests();
		return ResponseEntity.ok(ProgressDTO.of(subquests, subProgress, percentage));
	}

	@GetMapping("/participants/{id}/avatar")
	public void getAvatar(@PathVariable long id, Authentication auth, HttpServletResponse res) throws IOException,
			MinioException {
		Optional<QuestParticipant> participant = participants.findById(id);
		if (participant.isEmpty()) {
			res.setStatus(404);
			return;
		}
		if (!security.hasAccessToTheGroup(auth.getPrincipal(), participant.get()
		                                                                  .getGroup())) {
			res.setStatus(401);
			return;
		}
		String filename = "avatars/participants/" + String.valueOf(participant.get().getId());

		try {
			InputStream inputStream = minioService.get(Path.of(filename));
			InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

			// Set the content type and attachment header.
			res.addHeader("Content-disposition", "attachment;filename=" + filename);
			res.setContentType(URLConnection.guessContentTypeFromStream(inputStream));

			// Copy the stream to the response's output stream.
			IOUtils.copy(inputStream, res.getOutputStream());
			res.flushBuffer();
		} catch (MinioException ignored) {}
	}

	@PutMapping("participants/{id}/avatar")
	@PreAuthorize("hasRole('ROLE_PARTICIPANT')")
	public ResponseEntity<?> updateAvatar(@RequestParam("avatar") MultipartFile newAvatar, @PathVariable long id,
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

		String filename = "avatars/participants/" + String.valueOf(participantObj.getId());

		try {
			if (!Objects.requireNonNull(newAvatar.getContentType()).startsWith("image/")) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			minioService.upload(Path.of(filename), newAvatar.getInputStream(), newAvatar.getContentType());
			return new ResponseEntity<>(HttpStatus.OK);
		} catch (MinioException e) {
			throw new IllegalStateException("The file cannot be upload on the internal storage. Please retry later",
			                                e);
		} catch (IOException e) {
			throw new IllegalStateException("The file cannot be read", e);
		}
	}
}
