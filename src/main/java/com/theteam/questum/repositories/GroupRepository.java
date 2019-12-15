package com.theteam.questum.repositories;

import com.theteam.questum.models.QuestGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<QuestGroup, Long> {
}
