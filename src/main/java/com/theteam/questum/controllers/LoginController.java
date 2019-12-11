package com.theteam.questum.controllers;

import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.models.OwnerAuthToken;
import com.theteam.questum.repositories.GroupOwnersRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.repositories.OwnerAuthTokensRepository;
import com.theteam.questum.requests.LoginRequest;
import com.theteam.questum.responses.OwnerLoginResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
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
	private final OwnerAuthTokensRepository tokens;

	public LoginController(GroupRepository groups, GroupOwnersRepository owners, OwnerAuthTokensRepository tokens) {
		this.groups = groups;
		this.owners = owners;
		this.tokens = tokens;
	}

	@PutMapping("/login/owner")
	@PreAuthorize("permitAll()")
	public ResponseEntity<OwnerLoginResponse> login(@RequestBody LoginRequest req) {
		if (req.getRefreshToken() != null) {
			// TODO: implement auth with refresh token
			return null;
		} else if (!req.getEmail().equals("") && !req.getPassword().equals("")) {
			Optional<GroupOwner> groupOwner = owners.findByEmail(req.getEmail());
			if(groupOwner.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			if(!groupOwner.get().getPassword().equals(req.getPassword())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			OwnerLoginResponse res = groupOwner.map(owner -> {
				Optional<OwnerAuthToken> token = tokens.findByOwner_Id(owner.getId());
				if (token.isPresent()) {
					OwnerAuthToken ownerAuthToken = token.get();
					if(ownerAuthToken.getExpirationDate().toInstant().isBefore(Instant.now())) {
						tokens.delete(ownerAuthToken);
						return getOwnerLoginResponse(owner);
					} else {
						return new OwnerLoginResponse(ownerAuthToken.getToken(), ownerAuthToken.getOwner());
					}
				} else {
					return getOwnerLoginResponse(owner);
				}
			}).get();
			return ResponseEntity.ok(res);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	private OwnerLoginResponse getOwnerLoginResponse(GroupOwner owner) {
		UUID uuid = UUID.randomUUID();
		Timestamp expirationDate = Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS));
		OwnerAuthToken newToken = new OwnerAuthToken();
		newToken.setToken(uuid);
		newToken.setOwner(owner);
		newToken.setExpirationDate(expirationDate);
		tokens.save(newToken);
		return new OwnerLoginResponse(uuid, owner);
	}
}
