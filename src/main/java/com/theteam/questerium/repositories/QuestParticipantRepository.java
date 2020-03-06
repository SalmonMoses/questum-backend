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

	@Query(value = "SELECT count(*)\n" +
			"FROM quests q\n" +
			"         JOIN subquests sub ON sub.quest_id = q.id\n" +
			"         JOIN participants p ON p.group_id = q.group_id\n" +
			"         LEFT JOIN completed_subquests cq\n" +
			"                   ON (p.id = cq.user_id AND sub.id = cq.subquest_id)\n" +
			"WHERE p.id = 10\n" +
			"  AND q.id = 13\n" +
			"  AND (cq.verified IS NULL OR cq.verified = 0)", nativeQuery = true)
	long getRemainingSubquestsForQuestId(long userId, long questId);
}
