package com.theteam.questerium.repositories;

import com.theteam.questerium.models.QuestParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestParticipantRepository extends JpaRepository<QuestParticipant, Long> {
	public boolean existsByEmailAndGroup_Id(String email, long id);
	public List<QuestParticipant> findAllByGroup_Id(Long groupId);
	public List<QuestParticipant> findAllByGroup_IdOrderByPointsDesc(Long groupId);
	public Optional<QuestParticipant> findByEmailAndGroup_Id(String email, Long groupId);
}
