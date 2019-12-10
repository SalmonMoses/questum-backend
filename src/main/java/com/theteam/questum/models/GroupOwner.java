package com.theteam.questum.models;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "owners")
@Data
public class GroupOwner {
	@Id
	@GeneratedValue
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

	@OneToMany(fetch = FetchType.LAZY)
	private List<Group> groups;

	public GroupOwner() {
	}
}
