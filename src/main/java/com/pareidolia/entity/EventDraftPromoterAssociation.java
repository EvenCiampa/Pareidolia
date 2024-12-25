package com.pareidolia.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "EventDraft_Promoter_Association")
// TODO join between EventDraft -- 1 to many --> EventDraftPromoterAssociation
// TODO join between EventDraftPromoterAssociation -- many to 1 --> Account
public class EventDraftPromoterAssociation {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;
	@Column(name = "id_event_draft", nullable = false)
	private Long idEventDraft;
	@Column(name = "id_promoter", nullable = false)
	private Long idPromoter;
	@CreationTimestamp
	@Column(name = "creation_time", nullable = false, updatable = false)
	private LocalDateTime creationTime;
	@UpdateTimestamp
	@Column(name = "last_update", nullable = false)
	private LocalDateTime lastUpdate;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EventDraftPromoterAssociation that)) return false;
		return Objects.equals(id, that.id) && Objects.equals(idEventDraft, that.idEventDraft) && Objects.equals(idPromoter, that.idPromoter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, idEventDraft, idPromoter);
	}
}
