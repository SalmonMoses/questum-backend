package com.theteam.questerium.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

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

	@Column(name = "is_sent")
	private boolean isSent;

	@Column(name = "created_at")
	@CreationTimestamp
	@NonNull
	private Timestamp createdAt;
}
