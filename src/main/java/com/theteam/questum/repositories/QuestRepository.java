package com.theteam.questum.repositories;

import com.theteam.questum.models.Quest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestRepository extends JpaRepository<Quest, Long> {
	public List<Quest> findAllByGroup_Id(long id);
}
