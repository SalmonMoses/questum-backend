package com.theteam.questerium.models;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "refresh_tokens")
public class RefreshToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "refresh_token")
	private String refreshToken;

	@Column(name = "expiration_date")
	private Timestamp expirationDate;

	@Column(name = "user_id")
	private Long owner;

	@Column(name="type")
	private String type;
}
