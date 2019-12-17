package com.theteam.questum.controllers;

import com.theteam.questum.dto.QuestGroupOwnerDTO;
import com.theteam.questum.models.AuthToken;
import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.models.RefreshToken;
import com.theteam.questum.repositories.GroupOwnersRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.repositories.RefreshTokenRepository;
import com.theteam.questum.repositories.TokenRepository;
import com.theteam.questum.requests.SignupRequest;
import com.theteam.questum.responses.OwnerSignupResponse;
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
	private final GroupOwnersRepository owners;
	@Autowired
	private final TokenRepository tokens;
	@Autowired
	private final RefreshTokenRepository refreshTokens;

	public SignupController(GroupRepository groups, GroupOwnersRepository owners, TokenRepository tokens,
	                        RefreshTokenRepository refreshTokens) {
		this.groups = groups;
		this.owners = owners;
		this.tokens = tokens;
		this.refreshTokens = refreshTokens;
	}

	@PostMapping("/admin")
	public ResponseEntity<OwnerSignupResponse> signup(@RequestBody SignupRequest req) {
		String email = req.getEmail();
		String name = req.getName();
		String password = req.getPassword();
		if(owners.findByEmail(req.getEmail()).isPresent()) {
			OwnerSignupResponse res = OwnerSignupResponse.ofError("Email is already taken!");
			return new ResponseEntity<>(res, HttpStatus.CONFLICT);
		}
		GroupOwner owner = new GroupOwner();
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
		newToken.setType("ADMIN");
		tokens.save(newToken);
		String refreshTokenStr = UUID.randomUUID().toString();
		Timestamp refreshExpirationDate = Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS));
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setRefreshToken(refreshTokenStr);
		refreshToken.setOwner(owner.getId());
		refreshToken.setExpirationDate(refreshExpirationDate);
		refreshToken.setType("ADMIN");
		refreshTokens.save(refreshToken);
		QuestGroupOwnerDTO dto = QuestGroupOwnerDTO.of(owner);
		OwnerSignupResponse res = new OwnerSignupResponse(token, refreshTokenStr, dto, "");
		return new ResponseEntity(res, HttpStatus.CREATED);
	}
}
