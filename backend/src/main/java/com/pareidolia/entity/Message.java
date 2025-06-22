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
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Message")
@EqualsAndHashCode(exclude = {"account", "event"})
public class Message {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;
	@Lob
	@Basic
	@Column(name = "message", nullable = false, length = 65535)
	private String message;
	@Basic
	@Column(name = "id_account", nullable = false)
	private Long idAccount;
	@Basic
	@Column(name = "id_event", nullable = false)
	private Long idEvent;

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
	@JoinColumn(name = "id_account", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "message_to_account"))
	private Account account;
	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "id_event", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "message_to_event"))
	private Event event;
}