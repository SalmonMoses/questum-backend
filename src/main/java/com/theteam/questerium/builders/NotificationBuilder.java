package com.theteam.questerium.builders;

import com.theteam.questerium.models.Notification;
import lombok.NonNull;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import java.io.IOException;
import java.util.Map;

@Component
public class NotificationBuilder {
	private enum NotificationTypes {
		COMPLETED_SUBQUEST_OWNER,
		COMPLETED_QUEST_OWNER,
		SENT_ANSWER,
		ANSWER_ACCEPTED,
		ANSWER_REJECTED
	}

	private String type;

	private long userId;

	@NonNull
	@Column(name = "user_type")
	private String userType;

	@NonNull
	private Map<String, String> content;

	public NotificationBuilder type(NotificationTypes type) {
		this.type = type.name();
		return this;
	}

	public NotificationBuilder userId(long id) {
		this.userId = id;
		return this;
	}

	public NotificationBuilder userType(String type) {
		this.userType = type;
		return this;
	}

	public NotificationBuilder addData(String key, String value) {
		this.content.put(key, value);
		return this;
	}

	public Notification build() {
		ObjectMapper mapper = new ObjectMapper();
		String content = "";
		try {
			content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.content);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Notification n = new Notification();
		n.setType(this.type);
		n.setUserId(this.userId);
		n.setUserType(this.userType);
		n.setContent(content);
		n.setRead(false);
		return n;
	}
}
