package com.theteam.questerium.services;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import com.theteam.questerium.models.QuestGroup;
import com.theteam.questerium.models.QuestGroupOwner;
import com.theteam.questerium.models.QuestParticipant;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {
	@Autowired
	private Environment env;

	private final String API_KEY;
	private Email sender = new Email("Questerium@questerium.live");
	private final String SIGN_UP_TMPLT_ID = "d-faf43ba3ce12489da4f196ba8d8ea18c";
	private final String USR_SIGN_UP_TMPLT_ID = "d-6154bf4dea994ba1b034239b9f0f199a";
	private final String RESET_PSWD_TMPLT_ID = "d-108e4d6b5a65434c8af83be602fdf3ca";

	public EmailService(Environment env) {
		API_KEY = env.getProperty("spring.sendgrid.api-key");
	}

	public void sendSignUpMessage(QuestGroupOwner owner) throws IOException {
		String subject = "Your sign up on Questerium";
		Mail mail = new Mail();
		mail.setFrom(sender);
		mail.setSubject(subject);
		mail.setTemplateId(SIGN_UP_TMPLT_ID);

		Personalization personalization = new Personalization();
		personalization.addTo(new Email(owner.getEmail()));
		personalization.addDynamicTemplateData("name", owner.getName());
		mail.addPersonalization(personalization);

		SendGrid sg = new SendGrid(API_KEY);
		Request request = new Request();
		request.setMethod(Method.POST);
		request.setEndpoint("mail/send");
		request.setBody(mail.build());
		Response response = sg.api(request);
		if(response.getBody().equals("")) {
			log.info("Successfully sent admin sign up message to " + owner.getEmail());
		} else {
			log.info(response.getBody());
		}
	}

	public void sendParticipantSignUpEmail(QuestParticipant participant, String password) throws IOException {
		@NonNull QuestGroup participantGroup = participant.getGroup();

		String subject = "You were added to the group on Questerium";
		Mail mail = new Mail();
		mail.setFrom(sender);
		mail.setSubject(subject);
		mail.setTemplateId(USR_SIGN_UP_TMPLT_ID);

		Personalization personalization = new Personalization();
		personalization.addTo(new Email(participant.getEmail()));
		personalization.addDynamicTemplateData("name", participant.getName());
		personalization.addDynamicTemplateData("owner_name", participantGroup.getOwner().getName());
		personalization.addDynamicTemplateData("group_name", participantGroup.getName());
		personalization.addDynamicTemplateData("group_id", participantGroup.getId());
		personalization.addDynamicTemplateData("email", participant.getEmail());
		personalization.addDynamicTemplateData("password", password);
		mail.addPersonalization(personalization);

		SendGrid sg = new SendGrid(API_KEY);
		Request request = new Request();
		request.setMethod(Method.POST);
		request.setEndpoint("mail/send");
		request.setBody(mail.build());
		Response response = sg.api(request);
		if(response.getBody().equals("")) {
			log.info("Successfully sent participant sign up message to " + participant.getEmail());
		} else {
			log.info(response.getBody());
		}
	}

	public void sendRestorePswdMessage(QuestGroupOwner owner, String pswd) throws IOException {
		String subject = "Reset your password on Questerium";
		Mail mail = new Mail();
		mail.setFrom(sender);
		mail.setSubject(subject);
		mail.setTemplateId(RESET_PSWD_TMPLT_ID);

		Personalization personalization = new Personalization();
		personalization.addTo(new Email(owner.getEmail()));
		personalization.addDynamicTemplateData("name", owner.getName());
		personalization.addDynamicTemplateData("pswd", pswd);
		mail.addPersonalization(personalization);

		SendGrid sg = new SendGrid(API_KEY);
		Request request = new Request();
		request.setMethod(Method.POST);
		request.setEndpoint("mail/send");
		request.setBody(mail.build());
		Response response = sg.api(request);
		if(response.getBody().equals("")) {
			log.info("Successfully sent admin restore admin password message to " + owner.getEmail());
		} else {
			log.info(response.getBody());
		}
	}

	public void sendRestorePswdMessage(QuestParticipant participant, String pswd) throws IOException {
		String subject = "Reset your password on Questerium";
		Mail mail = new Mail();
		mail.setFrom(sender);
		mail.setSubject(subject);
		mail.setTemplateId(RESET_PSWD_TMPLT_ID);

		Personalization personalization = new Personalization();
		personalization.addTo(new Email(participant.getEmail()));
		personalization.addDynamicTemplateData("name", participant.getName());
		personalization.addDynamicTemplateData("pswd", pswd);
		mail.addPersonalization(personalization);

		SendGrid sg = new SendGrid(API_KEY);
		Request request = new Request();
		request.setMethod(Method.POST);
		request.setEndpoint("mail/send");
		request.setBody(mail.build());
		Response response = sg.api(request);
		if(response.getBody().equals("")) {
			log.info("Successfully sent participant restore admin password message to " + participant.getEmail());
		} else {
			log.info(response.getBody());
		}
	}
}
