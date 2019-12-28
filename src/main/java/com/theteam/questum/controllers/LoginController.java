package com.theteam.questum.controllers;

import com.theteam.questum.dto.QuestGroupOwnerDTO;
import com.theteam.questum.models.AuthToken;
import com.theteam.questum.models.QuestGroupOwner;
import com.theteam.questum.models.RefreshToken;
import com.theteam.questum.repositories.GroupOwnerRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.repositories.RefreshTokenRepository;
import com.theteam.questum.repositories.TokenRepository;
import com.theteam.questum.requests.OwnerLoginRequest;
import com.theteam.questum.responses.OwnerLoginResponse;
import com.theteam.questum.services.SHA512Service;
import lombok.NonNull;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/login")
public class LoginController {
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

	public LoginController(GroupRepository groups, GroupOwnerRepository owners, TokenRepository tokens,
	                       RefreshTokenRepository refreshTokens, SHA512Service encryptor) {
		this.groups = groups;
		this.owners = owners;
		this.tokens = tokens;
		this.refreshTokens = refreshTokens;
		this.encryptor = encryptor;
	}

	@PostMapping("/owner")
	public ResponseEntity<OwnerLoginResponse> groupOwnerLogin(@RequestBody OwnerLoginRequest req) {
		if (req.getRefreshToken() != null) {
			Optional<RefreshToken> refTok = refreshTokens.findByRefreshToken(req.getRefreshToken());
			if (refTok.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			if (!refTok.get().getType().equals("OWNER")) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			Optional<QuestGroupOwner> owner = owners.findById(refTok.get().getOwner());
			return owner.map(own -> ResponseEntity.ok(getOwnerLoginResponse(owner.get())))
			            .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
		} else if (!req.getEmail().equals("")) {
			Optional<QuestGroupOwner> owner = owners.findByEmail(req.getEmail());
			if (owner.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			@NonNull String password = encryptor.saltAndEncrypt(req.getEmail(), req.getPassword());
			if (!password.equals(owner.get().getPassword())) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			return ResponseEntity.ok(getOwnerLoginResponse(owner.get()));
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	private OwnerLoginResponse getOwnerLoginResponse(QuestGroupOwner owner) {
		Optional<AuthToken> tok = tokens.findByOwnerAndType(owner.getId(), "OWNER");
		tok.ifPresent(tokens::delete);
		Optional<RefreshToken> refTok = refreshTokens.findByOwnerAndType(owner.getId(), "OWNER");
		refTok.ifPresent(refreshTokens::delete);
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
		return new OwnerLoginResponse(token, refreshTokenStr, dto);
	}
}
