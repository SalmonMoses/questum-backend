package com.theteam.questum.controllers;

import com.theteam.questum.dto.QuestGroupOwnerDTO;
import com.theteam.questum.models.AuthToken;
import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.repositories.GroupOwnersRepository;
import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.repositories.TokenRepository;
import com.theteam.questum.requests.OwnerLoginRequest;
import com.theteam.questum.requests.SignupRequest;
import com.theteam.questum.responses.OwnerLoginResponse;
import com.theteam.questum.responses.OwnerSignupResponse;
import javassist.tools.web.BadHttpRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;
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
	private final GroupOwnersRepository owners;
	@Autowired
	private final TokenRepository tokens;

	public LoginController(GroupRepository groups, GroupOwnersRepository owners, TokenRepository tokens) {
		this.groups = groups;
		this.owners = owners;
		this.tokens = tokens;
	}

	@PostMapping("/login/admin")
//	@PreAuthorize("permitAll()")
	public ResponseEntity<OwnerLoginResponse> login(@RequestBody OwnerLoginRequest req) {
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
			if (token.isEmpty()) {
				return ResponseEntity.ok(getOwnerLoginResponse(owner.get()));
			} else if (token.get().getExpirationDate().getTime() < System.currentTimeMillis()) {
				tokens.delete(token.get());
				return ResponseEntity.ok(getOwnerLoginResponse(owner.get()));
			} else {
				ResponseEntity<OwnerLoginResponse> res = ResponseEntity.ok(new OwnerLoginResponse(token.get()
				                                                                                       .getToken(),
				                                                                                  QuestGroupOwnerDTO
						                                                                                  .of(owner.get())));
				return res;
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
		newToken.setType("ADMIN");
		tokens.save(newToken);
		QuestGroupOwnerDTO dto = QuestGroupOwnerDTO.of(owner);
		return new OwnerLoginResponse(uuid, dto);
	}
}
