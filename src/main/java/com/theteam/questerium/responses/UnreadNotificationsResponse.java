package com.theteam.questerium.responses;

import com.theteam.questerium.dto.NotificationDTO;
import lombok.Value;

import java.util.List;

@Value
public class UnreadNotificationsResponse {
	List<NotificationDTO> notifications;
}
