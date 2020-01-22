package com.theteam.questum.repositories;

import com.theteam.questum.models.CompletedSubquest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletedSubquestsRepository extends JpaRepository<CompletedSubquest, Long> {
}
