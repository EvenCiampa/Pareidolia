package com.pareidolia.entity;

import com.pareidolia.state.DraftState;
import com.pareidolia.state.State;
import com.pareidolia.state.StateConverter;
import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"promoters"})
@Table(name = "Event", indexes = {
	@Index(name = "event_data_ora", columnList = "date,time")
})
public class Event {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;
	@Basic
	@Column(name = "title", nullable = false)
	private String title;
	@Lob
	@Basic
	@Column(name = "description", length = 65535)
	private String description;
	@Basic
	@Column(name = "image")
	private String image;
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

	@Builder.Default
	@Column(name = "state", nullable = false)
	@Convert(converter = StateConverter.class)
	private State state = State.fromString(DraftState.name, null);

	@ColumnDefault("CURRENT_TIMESTAMP(6)")
	@CreationTimestamp(source = SourceType.DB)
	@Column(name = "creation_time", nullable = false, updatable = false, length = 6)
	private LocalDateTime creationTime;
	@ColumnDefault("CURRENT_TIMESTAMP(6)")
	@UpdateTimestamp(source = SourceType.DB)
	@Column(name = "last_update", nullable = false, length = 6)
	private LocalDateTime lastUpdate;

	@ManyToMany(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinTable(name = "Event_Promoter_Association",
		joinColumns = @JoinColumn(name = "id_event", referencedColumnName = "id", insertable = false, updatable = false),
		inverseJoinColumns = @JoinColumn(name = "id_promoter", referencedColumnName = "id_promoter", insertable = false, updatable = false),
		foreignKey = @ForeignKey(name = "event_to_event_promoter_association"))
	private List<PromoterInfo> promoters;

	@PostLoad
	void initializeState() {
		this.state = State.fromString(state.getStateName(), this);
	}
}
