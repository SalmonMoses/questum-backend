package com.theteam.questerium.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.TokenKey;
import com.theteam.questerium.repositories.TokenKeyRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.JacksonSerializer;
import io.jsonwebtoken.security.Keys;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.security.KeyPair;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
	@Autowired
	private TokenKeyRepository tokenKeys;
	@Autowired
	private ObjectMapper objectMapper;

	public String makeOwnerAccessToken(QuestGroupOwner owner) {
		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.ES256);
		TokenKey publicKey = new TokenKey();
		publicKey.setKey(Base64.toBase64String(keyPair.getPublic().getEncoded()));
		System.out.println(Base64.toBase64String(keyPair.getPrivate().getEncoded()));
		tokenKeys.save(publicKey); // save public key for future decode
		Key secretKey = Keys.hmacShaKeyFor("9b88ab4b639fc4ba54d635b9c68c27e5".getBytes());
		Date issuedAt = new Date();
		return Jwts.builder()
		           .setHeaderParam("kid", publicKey.getId())
		           .setSubject(owner.getEmail())
		           .setIssuedAt(issuedAt)
		           .setExpiration(Date.from(issuedAt.toInstant().plus(1, ChronoUnit.DAYS)))
		           .claim("rol", "owner")
		           .claim("typ", "acc")
		           .signWith(keyPair.getPrivate())
		           .serializeToJsonWith(new JacksonSerializer(objectMapper))
		           .compact();
	}
}
