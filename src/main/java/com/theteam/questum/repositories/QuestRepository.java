package com.theteam.questum.repositories;

import com.theteam.questum.models.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuestRepository extends JpaRepository<Quest, Long> {
	public List<Quest> findAllByGroup_Id(long id);
	public Optional<Quest> findByIdAndGroup_Id(long id, long groupId);
}
