package com.theteam.questerium.repositories;

import com.theteam.questerium.models.CompletedQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompletedQuestRepository extends JpaRepository<CompletedQuest, Long> {
	List<CompletedQuest> findAllByUser_Id(long id);

	@Query("SELECT p.points FROM QuestParticipant p WHERE p.id = :id")
	long getParticipantScoreById(long id);
}
