package com.theteam.questum.services;

import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.models.OwnerAuthToken;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class TokenService {
	public OwnerAuthToken getNewOwnerAuthToken(GroupOwner owner) {
		UUID uuid = UUID.randomUUID();
		Timestamp expirationDate = Timestamp.from(Instant.now().plus(1, ChronoUnit.DAYS));
		OwnerAuthToken newToken = new OwnerAuthToken();
		newToken.setToken(uuid);
		newToken.setOwner(owner);
		newToken.setExpirationDate(expirationDate);
		return newToken;
	}
}
