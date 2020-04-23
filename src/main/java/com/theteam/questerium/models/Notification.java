package com.theteam.questerium.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@Entity
@Data
@Table(name = "notifications")
@NoArgsConstructor
public class Notification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "type")
	private String type;

	@Column(name = "user_id")
	private long userId;

	@NonNull
	@Column(name = "user_type")
	private String userType;

	@NonNull
	@Column(name = "content")
	private String content;

	@Column(name = "is_read")
	private boolean isRead;
}
