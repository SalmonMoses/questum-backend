package com.theteam.questerium.repositories;

import com.theteam.questerium.models.CompletedSubquest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompletedSubquestsRepository extends JpaRepository<CompletedSubquest, Long> {
}
