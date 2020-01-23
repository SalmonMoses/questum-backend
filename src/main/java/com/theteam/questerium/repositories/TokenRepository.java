package com.theteam.questerium.repositories;

import com.theteam.questerium.models.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<AuthToken, Long> {
	public Optional<AuthToken> findByToken(String token);
	public Optional<AuthToken> findByOwnerAndType(Long owner, String type);
}
