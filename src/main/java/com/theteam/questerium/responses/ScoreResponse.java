package com.theteam.questerium.responses;

import com.theteam.questerium.dto.ScoringDTO;
import lombok.Value;

import java.util.List;

@Value
public class ScoreResponse {
	long points;
	List<ScoringDTO> scorings;
}
