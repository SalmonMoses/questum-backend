package com.theteam.questum.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@Data
@Entity
@Table(name = "completed_subquests")
@NoArgsConstructor
public class CompletedSubquest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NonNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private QuestParticipant user;

	@NonNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "subquest_id")
	private Subquest subquest;

	@NonNull
	@Column(name = "answer")
	private String answer;

	@Column(name = "verified")
	private boolean verified;
}
