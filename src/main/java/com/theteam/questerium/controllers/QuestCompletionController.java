package com.theteam.questerium.controllers;

import com.google.api.client.util.IOUtils;
import com.jlefebure.spring.boot.minio.MinioException;
import com.jlefebure.spring.boot.minio.MinioService;
import com.theteam.questerium.dto.CompletedSubquestDTO;
import com.theteam.questerium.models.*;
import com.theteam.questerium.repositories.*;
import com.theteam.questerium.requests.SubmitQuestAnswerRequest;
import com.theteam.questerium.requests.VerifySubquestRequest;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
import com.theteam.questerium.services.SecurityService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
	private CompletedQuestsRepository completedQuests;
	@Autowired
	private MinioService minio;
	@Autowired
	private SecurityService security;

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
		if (maybeSub.get().getVerificationType().equals("NONE")) {
			if (!req.getAnswer().equals("")) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
		}
		completedSub.setUser(participant.get());
		completedSub.setSubquest(maybeSub.get());
		completedSub.setAnswer(req.getAnswer());
		completedSub.setVerified(maybeSub.get().getVerificationType().equals("NONE"));
		completedSubquests.save(completedSub);
		if (maybeSub.get().getVerificationType().equals("NONE")) {
			if (participants.getRemainingSubquestsForQuestId(userPrincipal.getId(), maybeSub.get()
			                                                                                .getParentQuest()
			                                                                                .getId()) == 0) {
				CompletedQuest cq = new CompletedQuest();
				cq.setUser(participant.get());
				cq.setQuest(maybeSub.get().getParentQuest());
				cq.setPoints(maybeSub.get().getParentQuest().getPoints());
				completedQuests.save(cq);
				participant.get().setPoints(participant.get().getPoints() + cq.getPoints());
				participants.save(participant.get());
			}
		}
		return ResponseEntity.ok(CompletedSubquestDTO.of(completedSub));
	}

	@PutMapping("groups/{group_id}/verify")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<CompletedSubquestDTO> verifyQuestAnswer(@PathVariable("group_id") long groupId,
	                                                              @RequestBody VerifySubquestRequest req,
	                                                              Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!group.get().getOwner().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		Optional<CompletedSubquest> completedSub = completedSubquests.findByUser_IdAndSubquest_Id(req.getUserId(),
		                                                                                          req.getSubquestId());
		if (completedSub.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		CompletedSubquest subquest = completedSub.get();
		if (req.isVerified()) {
			subquest.setVerified(true);
			completedSubquests.save(subquest);
			CompletedSubquestDTO res = CompletedSubquestDTO.of(subquest);
			@NonNull QuestParticipant user = subquest.getUser();
			if (participants.getRemainingSubquestsForQuestId(user.getId(), subquest.getSubquest()
			                                                                       .getParentQuest()
			                                                                       .getId()) == 0) {
				CompletedQuest cq = new CompletedQuest();
				cq.setUser(user);
				cq.setQuest(subquest.getSubquest().getParentQuest());
				cq.setPoints(subquest.getSubquest().getParentQuest().getPoints());
				completedQuests.save(cq);
				user.setPoints(user.getPoints() + cq.getPoints());
				participants.save(user);
			}
			return ResponseEntity.ok(res);
		} else {
			if (subquest.isVerified()) {
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
			completedSubquests.delete(subquest);
			return new ResponseEntity<>(HttpStatus.OK);
		}
	}

	@GetMapping("groups/{group_id}/verification")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public void getQuestAnswer(@PathVariable("group_id") long groupId,
	                           @RequestParam("verification_id") long verificationId, Authentication auth,
	                           HttpServletResponse res) throws MinioException, IOException {
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			res.setStatus(404);
			return;
		}
		if (security.hasAccessToTheGroup(auth.getPrincipal(), group.get())) {
			res.setStatus(401);
			return;
		}
		Optional<CompletedSubquest> completedSub = completedSubquests.findById(verificationId);
		if (completedSub.isEmpty()) {
			res.setStatus(404);
			return;
		}

		long filename = completedSub.get().getId();

		InputStream inputStream = minio.get(Path.of("verifications/" + filename));
		InputStreamResource inputStreamResource = new InputStreamResource(inputStream);

		res.addHeader("Content-disposition", "attachment;filename=" + filename);
		res.setContentType(URLConnection.guessContentTypeFromStream(inputStream));

		IOUtils.copy(inputStream, res.getOutputStream());
		res.flushBuffer();
	}

	@PutMapping("groups/{group_id}/reject")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<CompletedSubquestDTO> rejectQuestAnswer(@PathVariable("group_id") long groupId,
	                                                              @RequestParam("verification_id") long verificationId,
	                                                              Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
		Optional<QuestGroup> group = groups.findById(groupId);
		if (group.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!group.get().getOwner().equals(owner.get())) {
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
		return new ResponseEntity(HttpStatus.OK);
	}
}
