package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.QuestGroupOwnerDTO;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.repositories.*;
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
	                       TokenRepository tokens,
	                       RefreshTokenRepository refreshTokens, JwtService jwtService, SHA512Service encryptor) {
		this.groups = groups;
		this.owners = owners;
		this.participants = participants;
		this.jwtService = jwtService;
		this.encryptor = encryptor;
	}

	@PostMapping("/owner")
	public ResponseEntity<OwnerLoginResponse> groupOwnerLogin(@RequestBody OwnerLoginRequest req) {
		if (req.getRefreshToken() != null) {
			var claims = jwtService.parseOwnerRefreshToken(req.getRefreshToken());
			Optional<QuestGroupOwner> owner = owners.findByEmail(claims.getBody().getSubject());
			if(owner.isEmpty()) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
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
			return null;
		} else if (!req.getEmail().equals("") && req.getGroupId() > 0) {
			return null;
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
}
