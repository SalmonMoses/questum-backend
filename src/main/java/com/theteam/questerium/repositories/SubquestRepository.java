package com.theteam.questerium.repositories;

import com.theteam.questerium.models.Subquest;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubquestRepository extends JpaRepository<Subquest, Long> {
	List<Subquest> findSubquestsByParentQuest_IdAndOrderGreaterThan(Long parentQuest_id, @NonNull Long order);
}
