package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.QuestGroupOwnerDTO;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.requests.SignupRequest;
import com.theteam.questerium.responses.OwnerSignupResponse;
import com.theteam.questerium.services.EmailService;
import com.theteam.questerium.services.JwtService;
import com.theteam.questerium.services.SHA512Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/signup")
@Slf4j
public class SignupController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final SHA512Service encryptor;
	@Autowired
	private final EmailService emailService;
	@Autowired
	private final JwtService jwtService;

	public SignupController(GroupRepository groups, GroupOwnerRepository owners, SHA512Service encryptor, EmailService emailService,
	                        JwtService jwtService) {
		this.groups = groups;
		this.owners = owners;
		this.encryptor = encryptor;
		this.emailService = emailService;
		this.jwtService = jwtService;
	}

	@PostMapping("/owner")
	public ResponseEntity<OwnerSignupResponse> signup(@RequestBody SignupRequest req) {
		String email = req.getEmail();
		String name = req.getName();
		String password = encryptor.saltAndEncrypt(req.getEmail(), req.getPassword());
		if(owners.existsByEmail(req.getEmail())) {
			OwnerSignupResponse res = OwnerSignupResponse.ofError("Email is already taken!");
			return new ResponseEntity<>(res, HttpStatus.CONFLICT);
		}
		QuestGroupOwner owner = new QuestGroupOwner();
		owner.setEmail(email);
		owner.setName(name);
		owner.setPassword(password);
		owners.save(owner);
		String token = jwtService.makeOwnerAccessToken(owner);
		String refreshTokenStr = jwtService.makeOwnerRefreshToken(owner);
		QuestGroupOwnerDTO dto = QuestGroupOwnerDTO.of(owner);
		OwnerSignupResponse res = new OwnerSignupResponse(token, refreshTokenStr, dto, "");
		try {
			emailService.sendSignUpMessage(owner);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(res, HttpStatus.CREATED);
	}
}
