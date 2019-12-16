package com.theteam.questum.repositories;

import com.theteam.questum.models.QuestParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestParticipantRepository extends JpaRepository<QuestParticipant, Long> {
	public List<QuestParticipant> findByIdAndGroup_Id(long id, long groupId);
	public List<QuestParticipant> findAllByGroup_Id(Long groupId);
	public Optional<QuestParticipant> findByEmailAndGroup_Id(String email, Long groupId);
}
