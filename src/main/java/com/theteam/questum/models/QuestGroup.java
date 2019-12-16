package com.theteam.questum.models;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "quest_groups")
@Data
public class QuestGroup {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NonNull
	@Column(name = "name")
	private String name;

	@NonNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private GroupOwner owner;

	@NonNull
	@OneToMany(fetch = FetchType.LAZY)
	private List<QuestParticipant> participants;

	public QuestGroup() {

	}
}
