package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.QuestGroupOwnerDTO;
import com.theteam.questerium.models.AuthToken;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.RefreshToken;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.repositories.RefreshTokenRepository;
import com.theteam.questerium.repositories.TokenRepository;
import com.theteam.questerium.requests.SignupRequest;
import com.theteam.questerium.responses.OwnerSignupResponse;
import com.theteam.questerium.services.SHA512Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@RestController
@RequestMapping("/signup")
public class SignupController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final TokenRepository tokens;
	@Autowired
	private final RefreshTokenRepository refreshTokens;
	@Autowired
	private final SHA512Service encryptor;

	public SignupController(GroupRepository groups, GroupOwnerRepository owners, TokenRepository tokens,
	                        RefreshTokenRepository refreshTokens, SHA512Service encryptor) {
		this.groups = groups;
		this.owners = owners;
		this.tokens = tokens;
		this.refreshTokens = refreshTokens;
		this.encryptor = encryptor;
	}

	@PostMapping("/owner")
	public ResponseEntity<OwnerSignupResponse> signup(@RequestBody SignupRequest req) {
		String email = req.getEmail();
		String name = req.getName();
		String password = encryptor.saltAndEncrypt(req.getEmail(), req.getPassword());
		if(owners.existsByEmail(req.getEmail())) {
			OwnerSignupResponse res = OwnerSignupResponse.ofError("Email is already taken!");
			return new ResponseEntity<>(res, HttpStatus.CONFLICT);
		}
		QuestGroupOwner owner = new QuestGroupOwner();
		owner.setEmail(email);
		owner.setName(name);
		owner.setPassword(password);
		owners.save(owner);
		String token = UUID.randomUUID().toString();
		Timestamp expirationDate = Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS));
		AuthToken newToken = new AuthToken();
		newToken.setToken(token);
		newToken.setOwner(owner.getId());
		newToken.setExpirationDate(expirationDate);
		newToken.setType("OWNER");
		tokens.save(newToken);
		String refreshTokenStr = UUID.randomUUID().toString();
		Timestamp refreshExpirationDate = Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS));
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setRefreshToken(refreshTokenStr);
		refreshToken.setOwner(owner.getId());
		refreshToken.setExpirationDate(refreshExpirationDate);
		refreshToken.setType("OWNER");
		refreshTokens.save(refreshToken);
		QuestGroupOwnerDTO dto = QuestGroupOwnerDTO.of(owner);
		OwnerSignupResponse res = new OwnerSignupResponse(token, refreshTokenStr, dto, "");
		return new ResponseEntity<>(res, HttpStatus.CREATED);
	}
}
