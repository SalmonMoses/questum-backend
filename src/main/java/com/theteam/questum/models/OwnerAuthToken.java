package com.theteam.questum.models;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "owner_tokens")
@Data
public class OwnerAuthToken {
	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "token")
	@NonNull
	private UUID token;

	@OneToOne
	@JoinColumn(name = "user_id", referencedColumnName = "id")
	@NonNull
	private GroupOwner owner;

	@NonNull
	@Column(name = "expiration_date")
	private Timestamp expirationDate;

	public OwnerAuthToken() {
	}
}
