package com.theteam.questerium.security;

import com.theteam.questerium.models.TokenKey;
import com.theteam.questerium.repositories.TokenKeyRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Optional;

@Component
public class JwtTokenKeyResolver extends SigningKeyResolverAdapter {
	@Autowired
	private TokenKeyRepository tokenKeys;

	@Override
	public Key resolveSigningKey(JwsHeader header, Claims claims) {
		Optional<TokenKey> key = tokenKeys.findById(Long.valueOf(header.getKeyId()));
		if (key.isEmpty()) {
			return null;
		}
		try {
			KeyFactory factory = KeyFactory.getInstance("EC");
			X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(key.get().getKey().getBytes());
			return factory.generatePublic(encodedKeySpec);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}
}
