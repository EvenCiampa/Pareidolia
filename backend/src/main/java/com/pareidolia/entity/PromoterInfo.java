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
@EqualsAndHashCode(exclude = {"account"})
@Table(name = "PromoterInfo", uniqueConstraints = {
	@UniqueConstraint(name = "UK_promoterInfo_id_promoter", columnNames = {"id_promoter"})
})
public class PromoterInfo {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	@Column(name = "id")
	private Long id;
	@Column(name = "id_promoter")
	private Long idPromoter;
	@Column(name = "photo")
	private String photo;
	@Lob
	@Column(name = "presentation", length = 65535)
	private String presentation;

	@ColumnDefault("CURRENT_TIMESTAMP(6)")
	@CreationTimestamp(source = SourceType.DB)
	@Column(name = "creation_time", nullable = false, updatable = false, length = 6)
	private LocalDateTime creationTime;
	@ColumnDefault("CURRENT_TIMESTAMP(6)")
	@UpdateTimestamp(source = SourceType.DB)
	@Column(name = "last_update", nullable = false, length = 6)
	private LocalDateTime lastUpdate;

	@OneToOne(fetch = FetchType.EAGER)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "id_promoter", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "promoter_info_to_account"))
	private Account account;
}
