package com.theteam.questerium.repositories;

import com.theteam.questerium.models.CompletedSubquest;
import com.theteam.questerium.models.QuestGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GroupRepository extends JpaRepository<QuestGroup, Long> {
	public List<QuestGroup> findAllByOwner_Id(long id);

	@Query("SELECT s FROM CompletedSubquest s WHERE s.subquest.parentQuest.group.id = :id AND s.verified = false")
	List<CompletedSubquest> findAllPendingSubquestsByGroup_Id(long id);
}
