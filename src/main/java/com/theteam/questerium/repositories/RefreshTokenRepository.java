package com.theteam.questerium.repositories;

import com.theteam.questerium.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
	public Optional<RefreshToken> findByRefreshToken(String token);
	public Optional<RefreshToken> findByOwnerAndType(Long owner, String type);
}
