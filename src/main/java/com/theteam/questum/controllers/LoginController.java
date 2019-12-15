package com.theteam.questum.controllers;

import com.theteam.questum.dto.QuestGroupDTO;
import com.theteam.questum.dto.QuestGroupOwnerDTO;
import com.theteam.questum.models.AuthToken;
import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.repositories.GroupOwnersRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.repositories.TokenRepository;
import com.theteam.questum.requests.LoginRequest;
import com.theteam.questum.responses.OwnerLoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@RestController
public class LoginController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnersRepository owners;
	@Autowired
	private final TokenRepository tokens;

	public LoginController(GroupRepository groups, GroupOwnersRepository owners, TokenRepository tokens) {
		this.groups = groups;
		this.owners = owners;
		this.tokens = tokens;
	}

	@PostMapping("/login/admin")
	@PreAuthorize("permitAll()")
	public ResponseEntity<OwnerLoginResponse> login(@RequestBody LoginRequest req) {
		if (req.getRefreshToken() != null) {
			// TODO: implement auth with refresh token
			return null;
		} else if (!req.getEmail().equals("") && !req.getPassword().equals("")) {
			Optional<GroupOwner> owner = owners.findByEmail(req.getEmail());
			if (owner.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			if (!owner.get().getPassword().equals(req.getPassword())) {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
			Optional<AuthToken> token = tokens.findByOwnerAndType(owner.get().getId(), "ADMIN");
			if (token.isEmpty() || token.get().getExpirationDate().getTime() < System.currentTimeMillis()) {
				return ResponseEntity.ok(getOwnerLoginResponse(owner.get()));
			} else {
				return ResponseEntity.ok(new OwnerLoginResponse(token.get().getToken(), QuestGroupOwnerDTO.of(owner.get())));
			}
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	private OwnerLoginResponse getOwnerLoginResponse(GroupOwner owner) {
		String uuid = UUID.randomUUID().toString();
		Timestamp expirationDate = Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS));
		AuthToken newToken = new AuthToken();
		newToken.setToken(uuid);
		newToken.setOwner(owner.getId());
		newToken.setExpirationDate(expirationDate);
		tokens.save(newToken);
		QuestGroupOwnerDTO dto = QuestGroupOwnerDTO.of(owner);
		return new OwnerLoginResponse(uuid, dto);
	}
}
