package com.theteam.questerium.repositories;

import com.theteam.questerium.models.CompletedQuest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CompletedQuestsRepository extends JpaRepository<CompletedQuest, Long> {
	List<CompletedQuest> findAllByUser_Id(long id);

	@Query("SELECT SUM(q.points) FROM CompletedQuest q WHERE q.user.id = ?1")
	long getParticipantScoreById(long id);
}
