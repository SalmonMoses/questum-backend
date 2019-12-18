package com.theteam.questum.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@Entity
@Data
@Table(name = "quests")
@NoArgsConstructor
public class Quest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "title")
	private String title;

	@NonNull
	@Column(name = "description")
	private String desc;

	@NonNull
	@JoinColumn(name = "group_id", referencedColumnName = "id")
	@ManyToOne
	private QuestGroup group;
}
