package com.theteam.questum.services;

import com.theteam.questum.models.AuthToken;
import com.theteam.questum.models.QuestGroup;
import com.theteam.questum.models.QuestGroupOwner;
import com.theteam.questum.models.QuestParticipant;
import com.theteam.questum.repositories.GroupOwnerRepository;
import com.theteam.questum.repositories.QuestParticipantRepository;
import com.theteam.questum.security.GroupOwnerPrincipal;
import com.theteam.questum.security.ParticipantPrincipal;
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
