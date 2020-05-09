package com.theteam.questerium.controllers;

import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.requests.AdminResetPasswordRequest;
import com.theteam.questerium.requests.ParticipantResetPasswordRequest;
import com.theteam.questerium.services.EmailService;
import com.theteam.questerium.services.PasswordGeneratorService;
import com.theteam.questerium.services.SHA512Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Optional;

@RestController
@Slf4j
public class RestorePasswordController {
	@Autowired
	private final QuestParticipantRepository participants;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final SHA512Service encryptor;
	@Autowired
	private final EmailService emailService;
	@Autowired
	private final PasswordGeneratorService pswdGeneratorService;

	public RestorePasswordController(QuestParticipantRepository participants, GroupOwnerRepository owners,
	                                 SHA512Service encryptor, EmailService emailService,
	                                 PasswordGeneratorService pswdGeneratorService) {
		this.participants = participants;
		this.owners = owners;
		this.encryptor = encryptor;
		this.emailService = emailService;
		this.pswdGeneratorService = pswdGeneratorService;
	}

	@PutMapping("/restore-pswd/owner")
	public ResponseEntity<?> restoreAdminPassword(@RequestBody AdminResetPasswordRequest req) {
		Optional<QuestGroupOwner> owner = owners.findByEmail(req.getEmail());
		if (owner.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		String newPswd = pswdGeneratorService.generateAlphanumericString();
		QuestGroupOwner questGroupOwner = owner.get();
		questGroupOwner.setPassword(encryptor.saltAndEncrypt(questGroupOwner.getEmail(), newPswd));
		owners.save(questGroupOwner);
		log.info("Owner #{} requested password reset", questGroupOwner.getId());
		try {
			emailService.sendRestorePswdMessage(questGroupOwner, newPswd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(null);
	}

	@PutMapping("/restore-pswd/participant")
	public ResponseEntity<?> restoreParticipantPassword(@RequestBody ParticipantResetPasswordRequest req) {
		Optional<QuestParticipant> owner = participants.findByEmailAndGroup_Id(req.getEmail(), req.getGroup());
		if (owner.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		String newPswd = pswdGeneratorService.generateAlphanumericString();
		QuestParticipant questParticipant = owner.get();
		questParticipant.setPassword(encryptor.saltAndEncrypt(questParticipant.getEmail(), newPswd));
		participants.save(questParticipant);
		log.info("Participant #{} (group #{}) requested password reset", questParticipant.getId(),
		         questParticipant.getGroup()
		                                                                                                           .getId());
		try {
			emailService.sendRestorePswdMessage(questParticipant, newPswd);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ResponseEntity.ok(null);
	}
}
