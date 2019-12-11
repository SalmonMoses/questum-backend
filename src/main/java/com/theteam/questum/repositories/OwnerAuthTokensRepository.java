package com.theteam.questum.repositories;

import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.models.OwnerAuthToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OwnerAuthTokensRepository extends JpaRepository<OwnerAuthToken, Long> {
	public Optional<OwnerAuthToken> findByToken(UUID token);
	public Optional<OwnerAuthToken> findByOwner_Id(Long id);
}
