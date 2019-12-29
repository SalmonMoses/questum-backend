package com.theteam.questum.models;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "owners")
@Data
public class QuestGroupOwner {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@NonNull
	@Column(name = "name")
	private String name;

	@NonNull
	@Column(name = "email")
	private String email;

	@NonNull
	@Column(name = "password")
	private String password;

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	private List<QuestGroup> questGroups;

	public QuestGroupOwner() {
	}
}
