package com.theteam.questum.services;

import com.theteam.questum.models.AuthToken;
import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.models.QuestGroup;
import com.theteam.questum.repositories.GroupOwnersRepository;
import com.theteam.questum.security.GroupOwnerDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestumAuthService {
	@Autowired
	GroupOwnersRepository owners;

	public GroupOwnerDetails handleOwnerLogin(AuthToken token) {
		GroupOwner owner = owners.findById(token.getOwner()).get();
		List<Long> groupIds = owner.getQuestGroups()
		                           .stream()
		                           .map(QuestGroup::getId)
		                           .collect(Collectors.toList());
		return new GroupOwnerDetails(owner.getEmail(), owner.getName(), groupIds);
	}
}
