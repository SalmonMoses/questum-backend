package com.theteam.questum.services;

import com.theteam.questum.models.AuthToken;
import com.theteam.questum.models.QuestGroupOwner;
import com.theteam.questum.models.QuestGroup;
import com.theteam.questum.repositories.GroupOwnerRepository;
import com.theteam.questum.security.GroupOwnerPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestumAuthService {
	@Autowired
	GroupOwnerRepository owners;

	public GroupOwnerPrincipal handleOwnerLogin(AuthToken token) {
		QuestGroupOwner owner = owners.findById(token.getOwner()).get();
		List<Long> groupIds = owner.getQuestGroups()
		                           .stream()
		                           .map(QuestGroup::getId)
		                           .collect(Collectors.toList());
		return new GroupOwnerPrincipal(owner.getEmail(), owner.getName(), groupIds);
	}
}
