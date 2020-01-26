package com.theteam.questerium.services;

import com.theteam.questerium.models.QuestGroup;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.repositories.GroupOwnerRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import com.theteam.questerium.security.GroupOwnerPrincipal;
import com.theteam.questerium.security.ParticipantPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {
	@Autowired
	private final GroupOwnerRepository owners;
	@Autowired
	private final QuestParticipantRepository participants;

	public SecurityService(GroupOwnerRepository owners, QuestParticipantRepository participants) {
		this.owners = owners;
		this.participants = participants;
	}

	public boolean hasAccessToTheGroup(Object principal, QuestGroup group) {
		// TODO: refactor with pattern matching in Java 14
		if (principal instanceof GroupOwnerPrincipal) {
			String ownerEmail = ((GroupOwnerPrincipal) principal).getEmail();
			Optional<QuestGroupOwner> owner = owners.findByEmail(ownerEmail);
			return owner.get().getQuestGroups().contains(group);
		} else if (principal instanceof ParticipantPrincipal) {
			String userEmail = ((ParticipantPrincipal) principal).getEmail();
			Optional<QuestParticipant> maybeUser = participants.findByEmailAndGroup_Id(userEmail, group.getId());
			return maybeUser.isPresent();
		}
		return false;
	}
}