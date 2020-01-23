package com.theteam.questerium.services;

import com.theteam.questerium.models.AuthToken;
import com.theteam.questerium.models.QuestGroup;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestumAuthService {
	@Autowired
	GroupOwnerRepository owners;

	@Autowired
	QuestParticipantRepository users;

	public GroupOwnerPrincipal handleOwnerLogin(AuthToken token) {
		QuestGroupOwner owner = owners.findById(token.getOwner()).get();
		List<Long> groupIds = owner.getQuestGroups()
		                           .stream()
		                           .map(QuestGroup::getId)
		                           .collect(Collectors.toList());
		return new GroupOwnerPrincipal(owner.getEmail(), owner.getName(), groupIds);
	}

	public ParticipantPrincipal handleUserLogin(AuthToken token) {
		QuestParticipant user = users.findById(token.getOwner()).get();
		return new ParticipantPrincipal(user.getEmail(), user.getName(), user.getGroup().getId());
	}
}
