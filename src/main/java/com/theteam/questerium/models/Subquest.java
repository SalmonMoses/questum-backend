package com.theteam.questerium.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@Data
@Entity
@Table(name = "subquests")
@NoArgsConstructor
public class Subquest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NonNull
	@Column(name = "description")
	private String description;

	@NonNull
	@Column(name = "verification_type")
	private String verificationType;

	@NonNull
	@Column(name = "order_num")
	private Long order;

	@NonNull
	@JoinColumn(name = "quest_id", referencedColumnName = "id")
	@ManyToOne(fetch = FetchType.EAGER)
	private Quest parentQuest;
}
