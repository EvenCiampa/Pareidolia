package com.pareidolia.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Event", indexes = {
	@Index(name = "event_id_utente", columnList = "id_promoter"),
	@Index(name = "event_data_ora", columnList = "date,time")
})
// TODO join between Event -- 1 to many --> EventPromoterAssociation
public class Event {
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
	@Column(name = "place", nullable = false)
	private String place;
	@Basic
	@Column(name = "date", nullable = false)
	private LocalDate date;
	@Basic
	@Column(name = "time", nullable = false)
	private LocalTime time;
	@Basic
	@Column(name = "duration", nullable = false)
	private Duration duration;
	@Basic
	@Column(name = "max_number_of_participants", nullable = false)
	private Long maxNumberOfParticipants;
	@Basic
	@Column(name = "average_score")
	private Double averageScore;
	@CreationTimestamp
	@Column(name = "creation_time", nullable = false, updatable = false)
	private LocalDateTime creationTime;
	@UpdateTimestamp
	@Column(name = "last_update", nullable = false)
	private LocalDateTime lastUpdate;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Event event)) return false;
		return Objects.equals(id, event.id) && Objects.equals(title, event.title) && Objects.equals(description, event.description) && Objects.equals(place, event.place) && Objects.equals(date, event.date) && Objects.equals(time, event.time) && Objects.equals(maxNumberOfParticipants, event.maxNumberOfParticipants) && Objects.equals(averageScore, event.averageScore);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title, description, place, date, time, maxNumberOfParticipants, averageScore);
	}
}
