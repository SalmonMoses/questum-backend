package com.theteam.questum.models;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
@Table(name = "tokens")
public class AuthToken {
	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "token")
	private String token;

	@Column(name = "expiration_date")
	private Timestamp expirationDate;

	@Column(name = "user_id")
	private Long owner;

	@Column(name="type")
	String type;
}
