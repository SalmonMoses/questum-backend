package com.theteam.questerium.controllers;

import com.google.api.client.util.IOUtils;
import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import com.theteam.questerium.dto.CompletedSubquestDTO;
import com.theteam.questerium.models.CompletedSubquest;
import com.theteam.questerium.models.QuestGroup;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.models.Subquest;
import com.theteam.questerium.repositories.*;
import com.theteam.questerium.requests.SubmitQuestAnswerRequest;
import com.theteam.questerium.requests.VerifySubquestRequest;
import com.theteam.questerium.security.ParticipantPrincipal;
import com.theteam.questerium.services.QuestService;
import com.theteam.questerium.services.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Optional;

@RestController
public class QuestCompletionController {
	@Autowired
	private final GroupRepository groups;
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final QuestParticipantRepository participants;
	@Autowired
	private final SubquestRepository subquests;
	@Autowired
	private final CompletedSubquestsRepository completedSubquests;
	@Autowired
	private CompletedQuestRepository completedQuests;
	@Autowired
	private QuestService questService;
	@Autowired
	private SecurityService security;
	@Autowired
	private MinioService minioService;

	public QuestCompletionController(GroupRepository groups, GroupOwnerRepository owners,
	                                 QuestParticipantRepository participants, SubquestRepository subquests,
	                                 CompletedSubquestsRepository completedSubquests) {
		this.groups = groups;
		this.owners = owners;
		this.participants = participants;
		this.subquests = subquests;
		this.completedSubquests = completedSubquests;
	}

	@PostMapping("/groups/{group_id}/submit")
	@PreAuthorize("hasRole('ROLE_PARTICIPANT')")
	public ResponseEntity<CompletedSubquestDTO> submitQuestAnswer(@PathVariable("group_id") long groupId,
	                                                              @RequestBody SubmitQuestAnswerRequest req,
	                                                              Authentication auth) {
		ParticipantPrincipal userPrincipal = (ParticipantPrincipal) auth.getPrincipal();
		if (groups.findById(groupId).isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		Optional<Subquest> maybeSub = subquests.findById(req.getSubquestId());
		if (maybeSub.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (maybeSub.get().getParentQuest().getGroup().getId() != groupId) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		CompletedSubquest completedSub = new CompletedSubquest();
		Optional<QuestParticipant> participant = participants.findById(userPrincipal.getId());
		if (!maybeSub.get().getVerificationType().equalsIgnoreCase("TEXT")) {
			if (!req.getAnswer().equals("")) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}
		completedSub.setUser(participant.get());
		completedSub.setSubquest(maybeSub.get());
		completedSub.setAnswer(req.getAnswer());
		completedSub.setVerified(false);
		if (maybeSub.get().getVerificationType().equalsIgnoreCase("NONE")) {
			completedSub.setVerified(true);
			completedSubquests.save(completedSub);
			questService.tryCompleteQuest(participant.get(), maybeSub.get().getParentQuest());
		} else if (maybeSub.get().getVerificationType().equalsIgnoreCase("TEXT") && req.getAnswer()
		                                                                               .equals(maybeSub.get()
		                                                                                               .getExpectedAnswer())) {
			completedSub.setVerified(true);
			completedSubquests.save(completedSub);
			questService.tryCompleteQuest(participant.get(), maybeSub.get().getParentQuest());
		} else {
			completedSubquests.save(completedSub);
		}
		return ResponseEntity.ok(CompletedSubquestDTO.of(completedSub));
	}

	@PutMapping("groups/{group_id}/submit")
	public ResponseEntity<?> submitQuestPhotoAnswer(@PathVariable("group_id") long groupId,
	                                                @RequestParam("verification_id") long verificationId,
	                                                @RequestParam("answer") MultipartFile answerFile,
	                                                Authentication auth) {
//		return ResponseEntity.ok(CompletedSubquestDTO.of(completedSub));
		return ResponseEntity.ok(null);
	}

	@PutMapping("groups/{group_id}/verify")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<CompletedSubquestDTO> verifyQuestAnswer(@PathVariable("group_id") long groupId,
	                                                              @RequestBody VerifySubquestRequest req,
	                                                              Authentication auth) {
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(auth.getPrincipal(), group.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Optional<CompletedSubquest> completedSub = completedSubquests.findByUser_IdAndSubquest_Id(req.getUserId(),
		                                                                                          req.getSubquestId());
		if (completedSub.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		CompletedSubquest subquest = completedSub.get();
		/*if (req.isVerified()) {
			subquest.setVerified(true);
			completedSubquests.save(subquest);
			CompletedSubquestDTO res = CompletedSubquestDTO.of(subquest);
			@NonNull QuestParticipant user = subquest.getUser();
			questService.tryCompleteQuest(user, subquest.getSubquest().getParentQuest());
			return ResponseEntity.ok(res);
		} else {
			if (subquest.isVerified()) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			completedSubquests.delete(subquest);
			return new ResponseEntity<>(HttpStatus.OK);
		}*/
		if (subquest.isVerified()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		subquest.setVerified(true);
		completedSubquests.save(subquest);
		CompletedSubquestDTO res = CompletedSubquestDTO.of(subquest);
		QuestParticipant user = subquest.getUser();
		questService.tryCompleteQuest(user, subquest.getSubquest().getParentQuest());
		return ResponseEntity.ok(res);
	}

	@PutMapping("groups/{group_id}/reject")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<CompletedSubquestDTO> rejectQuestAnswer(@PathVariable("group_id") long groupId,
	                                                              @RequestParam("verification_id") long verificationId,
	                                                              Authentication auth) {
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!security.hasAccessToTheGroup(auth.getPrincipal(), group.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Optional<CompletedSubquest> completedSub = completedSubquests.findById(verificationId);
		if (completedSub.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (completedSub.get().isVerified()) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
		CompletedSubquest subquest = completedSub.get();
		completedSubquests.delete(subquest);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/groups/{group_id}/answer")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public void getSubquestAnswer(@PathVariable("group_id") long groupId,
	                              @RequestParam("verification_id") long verificationId, Authentication auth,
	                              HttpServletResponse res) throws MinioException, IOException {
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			res.setStatus(404);
			return;
		}
		if (!security.hasAccessToTheGroup(auth.getPrincipal(), group.get())) {
			res.setStatus(401);
			return;
		}
		Optional<CompletedSubquest> cq = completedSubquests.findById(verificationId);
		if (cq.isEmpty()) {
			res.setStatus(404);
			return;
		}
		if (!cq.get().getSubquest().getParentQuest().getGroup().equals(group.get())) {
			res.setStatus(400);
			return;
		}
		switch (cq.get().getSubquest().getVerificationType()) {
			case "NONE":
				return;
			case "TEXT": {
				String answer = cq.get().getAnswer();
				res.setContentType("text/plain");
				res.getOutputStream().print(answer);
				res.flushBuffer();
				return;
			}
			case "IMAGE": {
				String filename = "answers/" + String.valueOf(cq.get().getId());

				InputStream inputStream = minioService.get(Path.of(filename));
				InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

				// Set the content type and attachment header.
				res.addHeader("Content-disposition", "attachment;filename=" + filename);
				res.setContentType(URLConnection.guessContentTypeFromStream(inputStream));

				// Copy the stream to the response's output stream.
				IOUtils.copy(inputStream, res.getOutputStream());
				res.flushBuffer();
				return;
			}
		}
	}
}
