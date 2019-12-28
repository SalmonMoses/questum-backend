package com.theteam.questum.controllers;

import com.theteam.questum.dto.QuestParticipantDTO;
import com.theteam.questum.repositories.QuestParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/participants")
public class ParticipantsController {
	@Autowired
	private QuestParticipantRepository participants;

	public ParticipantsController(QuestParticipantRepository participants) {
		this.participants = participants;
	}

	@GetMapping
	public ResponseEntity<List<QuestParticipantDTO>> getAllParticipants() {
		return ResponseEntity.ok(participants.findAll()
		                                     .stream()
		                                     .map(QuestParticipantDTO::of)
		                                     .collect(Collectors.toList()));
	}
}
