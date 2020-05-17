package com.theteam.questerium.services;

import com.theteam.questerium.models.QuestGroup;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestumAuthService {
	@Autowired
	GroupOwnerRepository owners;

	@Autowired
	QuestParticipantRepository participants;

	public GroupOwnerPrincipal handleOwnerLogin(Jws<Claims> token) {
		String subject = token.getBody().getSubject();
		long id;
		try {
			id = Long.parseLong(subject);
		} catch (NumberFormatException e) {
			throw new BadCredentialsException("Invalid JWToken");
		}
		QuestGroupOwner owner = owners.findById(id).get();
		List<Long> groupIds = owner.getQuestGroups()
		                           .stream()
		                           .map(QuestGroup::getId)
		                           .collect(Collectors.toList());
		return new GroupOwnerPrincipal(owner.getEmail(), owner.getName(), groupIds);
	}

	public ParticipantPrincipal handleUserLogin(Jws<Claims> token) {
		String subject = token.getBody().getSubject();
		long id;
		try {
			id = Long.parseLong(subject);
		} catch (NumberFormatException e) {
			throw new BadCredentialsException("Invalid JWToken");
		}
		QuestParticipant user = participants.findById(id).get();
		return new ParticipantPrincipal(user.getId(), user.getEmail(), user.getName(), user.getGroup().getId());
	}
}
