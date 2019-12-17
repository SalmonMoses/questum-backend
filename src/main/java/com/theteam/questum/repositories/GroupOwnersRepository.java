package com.theteam.questum.repositories;

import com.theteam.questum.models.GroupOwner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupOwnersRepository extends JpaRepository<GroupOwner, Long> {
	public Optional<GroupOwner> findByEmail(String email);
}
