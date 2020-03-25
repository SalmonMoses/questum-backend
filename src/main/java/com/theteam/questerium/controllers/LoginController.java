package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.QuestGroupOwnerDTO;
import com.theteam.questerium.dto.QuestParticipantDTO;
import com.theteam.questerium.models.*;
import com.theteam.questerium.repositories.*;
import com.theteam.questerium.requests.OwnerLoginRequest;
import com.theteam.questerium.requests.ParticipantLoginRequest;
import com.theteam.questerium.responses.OwnerLoginResponse;
import com.theteam.questerium.responses.ParticipantLoginResponse;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

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
	private final TokenRepository tokens;
	@Autowired
	private final RefreshTokenRepository refreshTokens;
	@Autowired
	private final SHA512Service encryptor;

	public LoginController(GroupRepository groups, GroupOwnerRepository owners, QuestParticipantRepository participants,
	                       TokenRepository tokens,
	                       RefreshTokenRepository refreshTokens, SHA512Service encryptor) {
		this.groups = groups;
		this.owners = owners;
		this.participants = participants;
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
			if(refTok.get().getExpirationDate().getTime() < System.currentTimeMillis()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			if (!refTok.get().getType().equals("OWNER")) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			Optional<QuestGroupOwner> owner = owners.findById(refTok.get().getOwner());
			return owner.map(own -> ResponseEntity.ok(getOwnerLoginResponse(owner.get(), true)))
			            .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
		} else if (!req.getEmail().equals("")) {
			Optional<QuestGroupOwner> owner = owners.findByEmail(req.getEmail());
			if (owner.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			@NonNull String password = encryptor.saltAndEncrypt(req.getEmail(), req.getPassword());
			if (!password.equalsIgnoreCase(owner.get().getPassword())) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			return ResponseEntity.ok(getOwnerLoginResponse(owner.get(), false));
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	@PostMapping("/user")
	public ResponseEntity<ParticipantLoginResponse> participantLogin(@RequestBody ParticipantLoginRequest req) {
		if (req.getRefreshToken() != null) {
			Optional<RefreshToken> refTok = refreshTokens.findByRefreshToken(req.getRefreshToken());
			if (refTok.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			if(refTok.get().getExpirationDate().getTime() < System.currentTimeMillis()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			if (!refTok.get().getType().equals("USER")) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			Optional<QuestParticipant> user = participants.findById(refTok.get().getOwner());
			return user.map(own -> ResponseEntity.ok(getParticipantLoginResponse(user.get(), true)))
			           .orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
		} else if (!req.getEmail().equals("") && req.getGroupId() > 0) {
			Optional<QuestGroup> group = groups.findById(req.getGroupId());
			if(group.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			Optional<QuestParticipant> user = participants.findByEmailAndGroup_Id(req.getEmail(), req.getGroupId());
			if(user.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			@NonNull String password = encryptor.saltAndEncrypt(req.getEmail(), req.getPassword());
			if (!password.equalsIgnoreCase(user.get().getPassword())) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			return ResponseEntity.ok(getParticipantLoginResponse(user.get(), false));
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	private OwnerLoginResponse getOwnerLoginResponse(QuestGroupOwner owner, boolean withRefTok) {
		Optional<AuthToken> tok = tokens.findByOwnerAndType(owner.getId(), "OWNER");
		tok.ifPresent(tokens::delete);
		Optional<RefreshToken> refTok = refreshTokens.findByOwnerAndType(owner.getId(), "OWNER");
		String token = UUID.randomUUID().toString();
		Timestamp expirationDate = Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS));
		AuthToken newToken = new AuthToken();
		newToken.setToken(token);
		newToken.setOwner(owner.getId());
		newToken.setExpirationDate(expirationDate);
		newToken.setType("OWNER");
		tokens.save(newToken);
		RefreshToken refreshToken = refTok.orElseGet(() -> {
			String refreshTokenStr = UUID.randomUUID().toString();
			Timestamp refreshExpirationDate = Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS));
			RefreshToken newRefreshToken = new RefreshToken();
			newRefreshToken.setRefreshToken(refreshTokenStr);
			newRefreshToken.setOwner(owner.getId());
			newRefreshToken.setExpirationDate(refreshExpirationDate);
			newRefreshToken.setType("OWNER");
			refreshTokens.save(newRefreshToken);
			return newRefreshToken;
		});
		if(refreshToken.getExpirationDate().getTime() < System.currentTimeMillis()) {
			if(!withRefTok) {
				String refreshTokenStr = UUID.randomUUID().toString();
				Timestamp refreshExpirationDate = Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS));
				refreshToken.setRefreshToken(refreshTokenStr);
				refreshToken.setExpirationDate(refreshExpirationDate);
				refreshTokens.save(refreshToken);
			} else {
				return null;
			}
		}
		QuestGroupOwnerDTO dto = QuestGroupOwnerDTO.of(owner);
		return new OwnerLoginResponse(token, refreshToken.getRefreshToken(), dto);
	}

	private ParticipantLoginResponse getParticipantLoginResponse(QuestParticipant user, boolean withRefTok) {
		Optional<AuthToken> tok = tokens.findByOwnerAndType(user.getId(), "USER");
		tok.ifPresent(tokens::delete);
		Optional<RefreshToken> refTok = refreshTokens.findByOwnerAndType(user.getId(), "USER");
		String token = UUID.randomUUID().toString();
		Timestamp expirationDate = Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS));
		AuthToken newToken = new AuthToken();
		newToken.setToken(token);
		newToken.setOwner(user.getId());
		newToken.setExpirationDate(expirationDate);
		newToken.setType("USER");
		tokens.save(newToken);
		RefreshToken refreshToken = refTok.orElseGet(() -> {
			String refreshTokenStr = UUID.randomUUID().toString();
			Timestamp refreshExpirationDate = Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS));
			RefreshToken newRefreshToken = new RefreshToken();
			newRefreshToken.setRefreshToken(refreshTokenStr);
			newRefreshToken.setOwner(user.getId());
			newRefreshToken.setExpirationDate(refreshExpirationDate);
			newRefreshToken.setType("OWNER");
			refreshTokens.save(newRefreshToken);
			return newRefreshToken;
		});
		if(refreshToken.getExpirationDate().getTime() < System.currentTimeMillis()) {
			if(!withRefTok) {
				String refreshTokenStr = UUID.randomUUID().toString();
				Timestamp refreshExpirationDate = Timestamp.from(Instant.now().plus(30, ChronoUnit.DAYS));
				refreshToken.setRefreshToken(refreshTokenStr);
				refreshToken.setExpirationDate(refreshExpirationDate);
				refreshTokens.save(refreshToken);
			} else {
				return null;
			}
		}
		QuestParticipantDTO dto = QuestParticipantDTO.of(user);
		return new ParticipantLoginResponse(token, refreshToken.getRefreshToken(), dto);
	}
}
