package com.theteam.questerium.repositories;

import com.theteam.questerium.models.Subquest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubquestRepository extends JpaRepository<Subquest, Long> {
}
