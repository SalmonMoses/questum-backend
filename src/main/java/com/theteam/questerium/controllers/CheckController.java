package com.theteam.questerium.controllers;

import com.google.api.client.util.IOUtils;
import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.GroupRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.responses.GroupCheckResponse;
import com.theteam.questerium.responses.ParticipantCheckResponse;
import com.theteam.questerium.responses.PasswordCheckResponse;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
import com.theteam.questerium.services.SHA512Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Optional;

@RestController
@RequestMapping("/check")
public class CheckController {
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final SHA512Service encrypter;
	@Autowired
	private GroupRepository groups;
	@Autowired
	private QuestParticipantRepository participants;
	@Autowired
	private MinioService minioService;

	public CheckController(GroupRepository groups, QuestParticipantRepository participants,
	                       GroupOwnerRepository owners, SHA512Service encryptor) {
		this.groups = groups;
		this.participants = participants;
		this.owners = owners;
		this.encrypter = encryptor;
	}

	@GetMapping("/group")
	public ResponseEntity<GroupCheckResponse> checkGroup(@RequestParam long id) {
		boolean exists = groups.existsById(id);
		GroupCheckResponse res = GroupCheckResponse.builder().type("group").id(id).exists(exists).build();
		return ResponseEntity.ok(res);
	}

	@GetMapping("/participant")
	public ResponseEntity<ParticipantCheckResponse> checkParticipant(@RequestParam long groupId,
	                                                                 @RequestParam String email) {
		boolean exists = groups.existsById(groupId);
		ParticipantCheckResponse res = ParticipantCheckResponse.builder()
		                                                       .type("participant")
		                                                       .groupId(groupId)
		                                                       .email(email)
		                                                       .exists(exists)
		                                                       .build();
		return ResponseEntity.ok(res);
	}

	@GetMapping("/password")
	public ResponseEntity<PasswordCheckResponse> checkPassword(@RequestParam String hash,
	                                                           Authentication auth) {
		Object principal = auth.getPrincipal();
		if (principal instanceof GroupOwnerPrincipal) {
			GroupOwnerPrincipal ownerPrincipal = (GroupOwnerPrincipal) principal;
			Optional<QuestGroupOwner> maybeOwner = owners.findByEmail(ownerPrincipal.getEmail());
			if (maybeOwner.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			PasswordCheckResponse res = PasswordCheckResponse.builder()
			                                                 .correct(hash.equalsIgnoreCase(maybeOwner.get()
			                                                                                                      .getPassword()))
			                                                 .build();
			return ResponseEntity.ok(res);
		} else if (principal instanceof ParticipantPrincipal) {
			ParticipantPrincipal participantPrincipal = (ParticipantPrincipal) principal;
			Optional<QuestParticipant> maybeParticipant = participants.findById(participantPrincipal.getId());
			if (maybeParticipant.isEmpty()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			PasswordCheckResponse res = PasswordCheckResponse.builder()
			                                                 .correct(hash.equalsIgnoreCase(maybeParticipant
					                                                                                            .get()
					                                                                                            .getPassword()))
			                                                 .build();
			return ResponseEntity.ok(res);
		} else {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/minio")
	public void checkMinio(HttpServletResponse res) throws MinioException, IOException {
		InputStream inputStream = minioService.get(Path.of("test.png"));
		InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

		// Set the content type and attachment header.
		res.addHeader("Content-disposition", "attachment;filename=test.png");
		res.setContentType(URLConnection.guessContentTypeFromName("test.png"));

		// Copy the stream to the response's output stream.
		IOUtils.copy(inputStream, res.getOutputStream());
		res.flushBuffer();
	}
}
