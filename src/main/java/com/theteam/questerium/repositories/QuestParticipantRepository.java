package com.theteam.questerium.repositories;

import com.theteam.questerium.models.QuestParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestParticipantRepository extends JpaRepository<QuestParticipant, Long> {
	boolean existsByEmailAndGroup_Id(String email, long id);

	List<QuestParticipant> findAllByGroup_Id(Long groupId);

	List<QuestParticipant> findAllByGroup_IdOrderByPointsDesc(Long groupId);

	Optional<QuestParticipant> findByEmailAndGroup_Id(String email, Long groupId);

	@Query("SELECT count(q) from CompletedSubquest q where q.user.id = :userId and q.subquest.parentQuest.id = " +
			":questId and q.verified = true")
	long getProgressForQuest(long userId, long questId);
}
