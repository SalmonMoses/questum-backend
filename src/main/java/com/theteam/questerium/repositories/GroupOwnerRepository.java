package com.theteam.questerium.repositories;

import com.theteam.questerium.models.QuestGroupOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupOwnerRepository extends JpaRepository<QuestGroupOwner, Long> {
	Optional<QuestGroupOwner> findByEmail(String email);
	boolean existsByEmail(String email);
}
