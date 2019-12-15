package com.theteam.questum.repositories;

import com.theteam.questum.models.AuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<AuthToken, Long> {
	public Optional<AuthToken> findByToken(String token);
	public Optional<AuthToken> findByOwnerAndType(Long owner, String type);
}
