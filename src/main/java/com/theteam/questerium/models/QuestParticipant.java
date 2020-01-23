package com.theteam.questerium.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@Data
@Entity
@Table(name = "participants")
@NoArgsConstructor
public class QuestParticipant {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "email")
	private String email;

	@NonNull
	@Column(name = "name")
	private String name;

	@NonNull
	@ManyToOne
	@JoinColumn(name = "group_id")
	private QuestGroup group;

	@NonNull
	@Column(name = "points")
	private Integer points;

	@NonNull
	@Column(name = "password")
	private String password;
}
