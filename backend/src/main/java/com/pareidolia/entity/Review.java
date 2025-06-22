package com.pareidolia.entity;

import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"account", "event"})
@Table(name = "Review")
public class Review {
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
	@Column(name = "score", nullable = false)
	private Long score;
	@Basic
	@Column(name = "id_consumer", nullable = false)
	private Long idConsumer;
	@Basic
	@Column(name = "id_event", nullable = false)
	private Long idEvent;
	@Basic
	@Column(name = "tag")
	private String tag;
	@Basic
	@Column(name = "is_anonymous", nullable = false)
	private boolean isAnonymous;

	@ColumnDefault("CURRENT_TIMESTAMP(6)")
	@CreationTimestamp(source = SourceType.DB)
	@Column(name = "creation_time", nullable = false, updatable = false, length = 6)
	private LocalDateTime creationTime;
	@ColumnDefault("CURRENT_TIMESTAMP(6)")
	@UpdateTimestamp(source = SourceType.DB)
	@Column(name = "last_update", nullable = false, length = 6)
	private LocalDateTime lastUpdate;

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "id_consumer", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "review_to_account"))
	private Account account;
	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "id_event", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "review_to_event"))
	private Event event;
}
