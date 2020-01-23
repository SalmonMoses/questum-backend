package com.theteam.questerium.repositories;

import com.theteam.questerium.models.QuestGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<QuestGroup, Long> {
	public List<QuestGroup> findAllByOwner_Id(long id);
}
