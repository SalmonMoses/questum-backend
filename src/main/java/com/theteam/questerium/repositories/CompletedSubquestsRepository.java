package com.theteam.questerium.repositories;

import com.theteam.questerium.models.CompletedSubquest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CompletedSubquestsRepository extends JpaRepository<CompletedSubquest, Long> {
	public Optional<CompletedSubquest> findByUser_IdAndSubquest_Id(long userId, long subquestId);
	@Query("SELECT q FROM CompletedSubquest q WHERE q.user.id = :userId AND q.subquest.parentQuest.id = " +
			":questId")
	public List<CompletedSubquest> findByUser_IdAndQuest_Id(long userId, long questId);
}
