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
@EqualsAndHashCode(exclude = {"event", "promoter"})
@Table(name = "Event_Promoter_Association")
public class EventPromoterAssociation {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;
	@Column(name = "id_event", nullable = false)
	private Long idEvent;
	@Column(name = "id_promoter", nullable = false)
	private Long idPromoter;

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
	@JoinColumn(name = "id_event", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "event_promoter_association_to_event"))
	private Event event;
	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "id_promoter", referencedColumnName = "id_promoter", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "event_promoter_association_to_promoter_info"))
	private PromoterInfo promoter;
}
