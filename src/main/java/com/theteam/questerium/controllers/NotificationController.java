package com.theteam.questerium.controllers;

import com.theteam.questerium.dto.NotificationDTO;
import com.theteam.questerium.models.Notification;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.NotificationRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.requests.MarkReadRequest;
import com.theteam.questerium.responses.UnreadNotificationsResponse;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
	@Autowired
	private GroupOwnerRepository owners;
	@Autowired
	private QuestParticipantRepository participants;
	@Autowired
	private NotificationRepository notifications;

	@GetMapping("/owner/{id}")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<UnreadNotificationsResponse> getAllUnreadNotificationsForOwner(@PathVariable long id,
	                                                                                     Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> user = owners.findByEmail(ownerEmail);
		Optional<QuestGroupOwner> owner = owners.findById(id);
		if (owner.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!user.get().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		List<Notification> unreadNotifications = notifications.findAllUnreadForUser(id, "OWNER");

		UnreadNotificationsResponse res = new UnreadNotificationsResponse(unreadNotifications.stream()
		                                                                                     .map(NotificationDTO::of)
		                                                                                     .collect(Collectors.toList()));
		return ResponseEntity.ok(res);
	}

	@PutMapping("/owner/{id}/markRead")
	@PreAuthorize("hasRole('ROLE_OWNER')")
	public ResponseEntity<?> markReadNotificationsForOwner(@PathVariable long id,
	                                                       @RequestBody MarkReadRequest req,
	                                                       Authentication auth) {
		String ownerEmail = ((GroupOwnerPrincipal) auth.getPrincipal()).getEmail();
		Optional<QuestGroupOwner> user = owners.findByEmail(ownerEmail);
		Optional<QuestGroupOwner> owner = owners.findById(id);
		if (owner.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (!user.get().equals(owner.get())) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		notifications.findAllById(req.getItems()).forEach(n -> {
			if(n.getUserId() != id || !n.getUserType().equalsIgnoreCase("owner")) return;
			n.setRead(true);
			notifications.save(n);
		});

		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping("/participants/{id}")
	@PreAuthorize("hasRole('ROLE_PARTICIPANT')")
	public ResponseEntity<UnreadNotificationsResponse> getAllUnreadNotificationsForParticipant(@PathVariable long id,
	                                                                                           Authentication auth) {
		ParticipantPrincipal principal = (ParticipantPrincipal) auth.getPrincipal();
		Optional<QuestParticipant> participant = participants.findById(id);
		if (participant.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		QuestParticipant participantObj = participant.get();
		if (principal.getId() != id) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		List<Notification> unreadNotifications = notifications.findAllUnreadForUser(id, "PARTICIPANT");

		UnreadNotificationsResponse res = new UnreadNotificationsResponse(unreadNotifications.stream()
		                                                                                     .map(NotificationDTO::of)
		                                                                                     .collect(Collectors.toList()));
		return ResponseEntity.ok(res);
	}

	@PutMapping("/participants/{id}/markRead")
	@PreAuthorize("hasRole('ROLE_PARTICIPANT')")
	public ResponseEntity<?> markReadNotificationsForParticipant(@PathVariable long id,
	                                                             @RequestBody MarkReadRequest req,
	                                                             Authentication auth) {
		ParticipantPrincipal principal = (ParticipantPrincipal) auth.getPrincipal();
		Optional<QuestParticipant> participant = participants.findById(id);
		if (participant.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		QuestParticipant participantObj = participant.get();
		if (principal.getId() != id) {
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}

		notifications.findAllById(req.getItems()).forEach(n -> {
			if(n.getUserId() != id || !n.getUserType().equalsIgnoreCase("participant")) return;
			n.setRead(true);
			notifications.save(n);
		});

		return new ResponseEntity<>(HttpStatus.OK);
	}
}
