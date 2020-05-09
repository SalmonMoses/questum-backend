package com.theteam.questerium.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theteam.questerium.models.Notification;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class NotificationDTO {
	private long id;
	private boolean isRead;

	@NonNull
	private String type;

	@NonNull
	private Map<String, Object> content;

	public static NotificationDTO of(Notification n) {
		ObjectMapper mapper = new ObjectMapper();

		Map<String, Object> content = null;
		try {
			content = mapper.readValue(n.getContent(),
			                           new TypeReference<Map<String, Object>>() {
			                           });
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return NotificationDTO.builder()
		                      .id(n.getId())
		                      .type(n.getType())
		                      .content(content)
		                      .isRead(n.isRead())
		                      .build();
	}
}
