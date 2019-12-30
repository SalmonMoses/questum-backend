package com.theteam.questum.repositories;

import com.theteam.questum.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	public Optional<RefreshToken> findByRefreshToken(String token);
	public Optional<RefreshToken> findByOwnerAndType(Long owner, String type);
}
