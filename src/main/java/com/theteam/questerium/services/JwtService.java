package com.theteam.questerium.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.repositories.TokenKeyRepository;
import com.theteam.questerium.security.JwtTokenKeyResolver;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.JacksonDeserializer;
import io.jsonwebtoken.io.JacksonSerializer;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
	@Autowired
	private TokenKeyRepository tokenKeys;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private JwtTokenKeyResolver keyResolver;

	private Key secret = Keys.hmacShaKeyFor("9b88ab4b639fc4ba54d635b9c68c27e5".getBytes());

	public String makeOwnerAccessToken(QuestGroupOwner owner) {
//		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.ES256);
//		TokenKey publicKey = new TokenKey();
//		publicKey.setKey(Base64.toBase64String(keyPair.getPublic().getEncoded()));
//		System.out.println(Base64.toBase64String(keyPair.getPrivate().getEncoded()));
//		tokenKeys.save(publicKey); // save public key for future decode
		Date issuedAt = new Date();
		return Jwts.builder()
//		           .setHeaderParam("kid", publicKey.getId())
		           .setSubject(owner.getEmail())
		           .setIssuedAt(issuedAt)
		           .setExpiration(Date.from(issuedAt.toInstant().plus(1, ChronoUnit.DAYS)))
		           .claim("rol", "owner")
		           .claim("typ", "acc")
		           .signWith(secret)
		           .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
		           .compact();
	}

	public String makeOwnerRefreshToken(QuestGroupOwner owner) {
//		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.ES256);
//		TokenKey publicKey = new TokenKey();
//		publicKey.setKey(Base64.toBase64String(keyPair.getPublic().getEncoded()));
//		System.out.println(Base64.toBase64String(keyPair.getPrivate().getEncoded()));
//		tokenKeys.save(publicKey); // save public key for future decode
		Date issuedAt = new Date();
		return Jwts.builder()
//		           .setHeaderParam(JwsHeader.KEY_ID, publicKey.getId())
		           .setSubject(owner.getEmail())
		           .setIssuedAt(issuedAt)
		           .setExpiration(Date.from(issuedAt.toInstant().plus(30, ChronoUnit.DAYS)))
		           .claim("rol", "owner")
		           .claim("typ", "ref")
		           .signWith(secret)
		           .serializeToJsonWith(new JacksonSerializer<>(objectMapper))
		           .compact();
	}

	public Jws<Claims> parseOwnerRefreshToken(String token) {
		return Jwts.parserBuilder()
//		           .setSigningKeyResolver(keyResolver)
		           .setSigningKey(secret)
		           .require("typ", "ref")
		           .require("rol", "owner")
		           .deserializeJsonWith(new JacksonDeserializer<>(objectMapper))
		           .build()
		           .parseClaimsJws(token);
	}

	public Jws<Claims> parseAccessToken(String token) {
		return Jwts.parserBuilder()
//		           .setSigningKeyResolver(keyResolver)
                   .setSigningKey(secret)
                   .require("typ", "acc")
                   .require("rol", "owner")
                   .deserializeJsonWith(new JacksonDeserializer<>(objectMapper))
                   .build()
                   .parseClaimsJws(token);
	}
}
