package com.theteam.questerium.services;

import com.theteam.questerium.builders.NotificationBuilder;
import com.theteam.questerium.models.Notification;
import com.theteam.questerium.models.Quest;
import com.theteam.questerium.models.QuestParticipant;
import com.theteam.questerium.models.Subquest;
import com.theteam.questerium.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class NotificationService {
	@Autowired
	private NotificationRepository notifications;

	public Notification sendSubquestCompletingNotification(QuestParticipant participant, Subquest subquest) {
		Notification n = new NotificationBuilder().type(NotificationBuilder.NotificationTypes.COMPLETED_SUBQUEST_OWNER)
		                                          .userId(subquest.getParentQuest().getGroup().getOwner().getId())
		                                          .userType("OWNER")
		                                          .addData("user", participant.getName())
		                                          .addData("subquest", subquest.getDescription())
		                                          .build();
		notifications.save(n);
		return n;
	}

	public Notification sendQuestCompletingNotification(QuestParticipant participant, Quest quest) {
		Notification n = new NotificationBuilder().type(NotificationBuilder.NotificationTypes.COMPLETED_QUEST_OWNER)
		                                          .userId(quest.getGroup().getOwner().getId())
		                                          .userType("OWNER")
		                                          .addData("user", participant.getName())
		                                          .addData("quest", quest.getTitle())
		                                          .build();
		notifications.save(n);
		return n;
	}

	public Notification sendSentAnswerNotification(QuestParticipant participant, Subquest subquest) {
		Notification n = new NotificationBuilder().type(NotificationBuilder.NotificationTypes.SENT_ANSWER)
		                                          .userId(subquest.getParentQuest().getGroup().getOwner().getId())
		                                          .userType("OWNER")
		                                          .addData("user", participant.getName())
		                                          .addData("subquest", subquest.getDescription())
		                                          .build();
		notifications.save(n);
		return n;
	}

	public Notification sendAnswerAcceptedNotification(QuestParticipant participant, Subquest subquest) {
		Notification n = new NotificationBuilder().type(NotificationBuilder.NotificationTypes.ANSWER_ACCEPTED)
		                                          .userId(participant.getId())
		                                          .userType("PARTICIPANT")
		                                          .addData("subquest", subquest.getDescription())
		                                          .build();
		notifications.save(n);
		return n;
	}

	public Notification sendAnswerRejectedNotification(QuestParticipant participant, Subquest subquest) {
		Notification n = new NotificationBuilder().type(NotificationBuilder.NotificationTypes.ANSWER_REJECTED)
		                                          .userId(participant.getId())
		                                          .userType("PARTICIPANT")
		                                          .addData("subquest", subquest.getDescription())
		                                          .build();
		notifications.save(n);
		return n;
	}

	public int deleteNotificationsCreatedBefore30Days() {
		Instant monthBefore = Instant.now().minus(30, ChronoUnit.DAYS);
		return notifications.deleteNotificationsCreatedBeforeNDays(Timestamp.from(monthBefore));
	}
}
