package com.theteam.questum.repositories;

import com.theteam.questum.models.Subquest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubquestRepository extends JpaRepository<Subquest, Long> {
}
