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
@Table(name = "Event_Promoter_Association")
// TODO join between Event -- 1 to many --> EventPromoterAssociation
// TODO join between EventPromoterAssociation -- many to 1 --> Account
public class EventPromoterAssociation {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;
	@Column(name = "id_event", nullable = false)
	private Long idEvent;
	@Column(name = "id_promoter", nullable = false)
	private Long idPromoter;
	@CreationTimestamp
	@Column(name = "creation_time", nullable = false, updatable = false)
	private LocalDateTime creationTime;
	@UpdateTimestamp
	@Column(name = "last_update", nullable = false)
	private LocalDateTime lastUpdate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_event", insertable = false, updatable = false)
	private Event event;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_promoter", insertable = false, updatable = false)
	private Account promoter;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof EventPromoterAssociation that)) return false;
		return Objects.equals(id, that.id) && Objects.equals(idEvent, that.idEvent) && Objects.equals(idPromoter, that.idPromoter);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, idEvent, idPromoter);
	}
}
