package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.QuestGroupOwnerDTO;
import com.theteam.questerium.dto.QuestParticipantDTO;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.requests.OwnerLoginRequest;
import com.theteam.questerium.requests.ParticipantLoginRequest;
import com.theteam.questerium.responses.OwnerLoginResponse;
import com.theteam.questerium.responses.ParticipantLoginResponse;
import com.theteam.questerium.services.JwtService;
import com.theteam.questerium.services.SHA512Service;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/login")
@Slf4j
public class LoginController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final QuestParticipantRepository participants;
	@Autowired
	private final JwtService jwtService;
	@Autowired
	private final SHA512Service encryptor;

	public LoginController(GroupRepository groups, GroupOwnerRepository owners,
	                       QuestParticipantRepository participants,
	                       JwtService jwtService, SHA512Service encryptor) {
		this.groups = groups;
		this.owners = owners;
		this.participants = participants;
		this.jwtService = jwtService;
		this.encryptor = encryptor;
	}

	@PostMapping("/owner")
	public ResponseEntity<OwnerLoginResponse> groupOwnerLogin(@RequestBody OwnerLoginRequest req) {
		if (req.getRefreshToken() != null) {
			var claims = jwtService.parseOwnerRefreshToken(req.getRefreshToken()).getBody();
			if (claims == null) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			String subject = claims.getSubject();
			long id;
			try {
				id = Long.parseLong(subject);
			} catch (NumberFormatException e) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			Optional<QuestGroupOwner> owner = owners.findById(id);
			if (owner.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			String jwtAccessToken = jwtService.makeOwnerAccessToken(owner.get());
			String jwtRefreshToken = jwtService.makeOwnerRefreshToken(owner.get());
			return ResponseEntity.ok(new OwnerLoginResponse(jwtAccessToken,
			                                                jwtRefreshToken,
			                                                QuestGroupOwnerDTO.of(owner.get())));
		} else if (!req.getEmail().equals("")) {
			Optional<QuestGroupOwner> owner = owners.findByEmail(req.getEmail());
			if (owner.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			@NonNull String password = encryptor.saltAndEncrypt(req.getEmail(), req.getPassword());
			if (!password.equalsIgnoreCase(owner.get().getPassword())) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			String jwtAccessToken = jwtService.makeOwnerAccessToken(owner.get());
			String jwtRefreshToken = jwtService.makeOwnerRefreshToken(owner.get());
			return ResponseEntity.ok(new OwnerLoginResponse(jwtAccessToken,
			                                                jwtRefreshToken,
			                                                QuestGroupOwnerDTO.of(owner.get())));
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	@PostMapping("/user")
	public ResponseEntity<ParticipantLoginResponse> participantLogin(@RequestBody ParticipantLoginRequest req) {
		if (req.getRefreshToken() != null) {
			var claims = jwtService.parseParticipantRefreshToken(req.getRefreshToken()).getBody();
			if (claims == null) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			String subject = claims.getSubject();
			long id;
			try {
				id = Long.parseLong(subject);
			} catch (NumberFormatException e) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			Optional<QuestParticipant> participant = participants.findById(id);
			if (participant.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			String jwtAccessToken = jwtService.makeParticipantAccessToken(participant.get());
			String jwtRefreshToken = jwtService.makeParticipantRefreshToken(participant.get());
			return ResponseEntity.ok(new ParticipantLoginResponse(jwtAccessToken,
			                                                      jwtRefreshToken,
			                                                      QuestParticipantDTO.of(participant.get())));
		} else if (!req.getEmail().equals("") && req.getGroupId() > 0) {
			Optional<QuestParticipant> participant = participants.findByEmailAndGroup_Id(req.getEmail(),
			                                                                             req.getGroupId());
			if (participant.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			@NonNull String password = encryptor.saltAndEncrypt(req.getEmail(), req.getPassword());
			if (!password.equalsIgnoreCase(participant.get().getPassword())) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			String jwtAccessToken = jwtService.makeParticipantAccessToken(participant.get());
			String jwtRefreshToken = jwtService.makeParticipantRefreshToken(participant.get());
			return ResponseEntity.ok(new ParticipantLoginResponse(jwtAccessToken,
			                                                      jwtRefreshToken,
			                                                      QuestParticipantDTO.of(participant.get())));
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
}
