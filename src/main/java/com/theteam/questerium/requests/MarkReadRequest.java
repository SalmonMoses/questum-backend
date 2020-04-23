package com.theteam.questerium.requests;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MarkReadRequest {
	List<Long> items;
}
