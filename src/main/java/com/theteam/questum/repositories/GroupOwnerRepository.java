package com.theteam.questum.repositories;

import com.theteam.questum.models.QuestGroupOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupOwnerRepository extends JpaRepository<QuestGroupOwner, Long> {
	public Optional<QuestGroupOwner> findByEmail(String email);
}
