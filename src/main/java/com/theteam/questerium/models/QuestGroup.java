package com.theteam.questerium.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "quest_groups")
@Data
@NoArgsConstructor
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
	private QuestGroupOwner owner;

	@NonNull
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "group")
	private List<QuestParticipant> participants;

	@NonNull
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "group")
	private List<Quest> quests;

	@PreRemove
	private void onRemove() {
		owner.getQuestGroups().remove(this);
	}
}
