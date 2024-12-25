package com.pareidolia.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Review")
public class Review {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;
	@Basic
	@Column(name = "title", nullable = false)
	private String title;
	@Basic
	@Column(name = "description", columnDefinition = "TEXT")
	private String description;
	@Basic
	@Column(name = "score", nullable = false)
	private Long score;
	@Basic
	@Column(name = "id_consumer", nullable = false)
	private Long idConsumer;
	@Basic
	@Column(name = "id_event", nullable = false)
	private Long idEvent;
	@CreationTimestamp
	@Column(name = "creation_time", nullable = false, updatable = false)
	private LocalDateTime creationTime;
	@UpdateTimestamp
	@Column(name = "last_update", nullable = false)
	private LocalDateTime lastUpdate;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Review review)) return false;
		return Objects.equals(id, review.id) && Objects.equals(title, review.title) && Objects.equals(description, review.description) && Objects.equals(score, review.score) && Objects.equals(idConsumer, review.idConsumer) && Objects.equals(idEvent, review.idEvent);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title, description, score, idConsumer, idEvent);
	}
}
