package com.theteam.questum.models;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;

@Entity
@Table(name = "groups")
@Data
public class Group {
	@Id
	@GeneratedValue
	private long id;

	@NonNull
	@Column(name = "name")
	private String name;

	@NonNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private GroupOwner owner;

	public Group() {

	}
}
