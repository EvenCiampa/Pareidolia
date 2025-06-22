package com.pareidolia.entity;

import jakarta.persistence.*;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
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
@Table(name = "Booking", indexes = {
	@Index(name = "booking_id_account", columnList = "id_account"),
	@Index(name = "booking_unique_key", columnList = "id_event,id_account", unique = true)
})
public class Booking {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;
	@Basic
	@Column(name = "id_event", nullable = false)
	private Long idEvent;
	@Basic
	@Column(name = "id_account", nullable = false)
	private Long idAccount;

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
	@JoinColumn(name = "id_account", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "booking_to_account"))
	private Account account;
	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "id_event", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "booking_to_event"))
	private Event event;
}
