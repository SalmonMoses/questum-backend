package com.theteam.questum.controllers;

import com.theteam.questum.repositories.GroupRepository;
import com.theteam.questum.responses.CheckResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.http.HttpResponse;

@RestController
@RequestMapping("/check")
public class CheckController {
	@Autowired
	GroupRepository groups;

	@GetMapping("/group/{id}")
	public ResponseEntity<CheckResponse> checkGroup(@PathVariable long id) {
		boolean exists = groups.existsById(id);
		CheckResponse res = CheckResponse.builder().type("group").id(id).exists(exists).build();
		return ResponseEntity.ok(res);
	}
}
