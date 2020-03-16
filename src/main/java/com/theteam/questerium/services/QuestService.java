package com.theteam.questerium.services;

import com.theteam.questerium.models.CompletedQuest;
import com.theteam.questerium.models.Quest;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.repositories.CompletedQuestRepository;
import com.theteam.questerium.repositories.QuestParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestService {
	@Autowired
	private QuestParticipantRepository participants;
	@Autowired
	private CompletedQuestRepository completedQuests;

	public void tryCompleteQuest(QuestParticipant participant, Quest quest) {
		long remainingSubquests = participants.getRemainingSubquestsForQuestId(participant.getId(),
		                                                                       quest.getId());
		if (remainingSubquests == 0) {
			CompletedQuest cq = new CompletedQuest();
			cq.setUser(participant);
			cq.setQuest(quest);
			cq.setPoints(quest.getPoints());
			completedQuests.save(cq);
			participant.setPoints(participant.getPoints() + cq.getPoints());
			participants.save(participant);
		}
	}
}
