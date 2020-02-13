package com.theteam.questerium.repositories;

import com.theteam.questerium.models.CompletedSubquest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompletedSubquestsRepository extends JpaRepository<CompletedSubquest, Long> {
	public Optional<CompletedSubquest> findByUser_IdAndSubquest_Id(long userId, long subquestId);
}
