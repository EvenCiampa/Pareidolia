package com.pareidolia.entity;

//questa è la tabella con le informazioni dellorganizzatore degli eventi, è una 1 a 0/1 con Account
// perchè solo se sei promoter puoi avere una pagina con info

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
@Table(name = "PromoterInfo")
public class PromoterInfo {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "id_promoter", unique = true)
    private Long idPromoter;
    @Column(name = "photo")
    private String photo;
    @Column(name = "presentation", columnDefinition = "TEXT")
    private String presentation;
	@CreationTimestamp
	@Column(name = "creation_time", nullable = false, updatable = false)
	private LocalDateTime creationTime;
	@UpdateTimestamp
	@Column(name = "last_update", nullable = false)
	private LocalDateTime lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromoterInfo that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(idPromoter, that.idPromoter) && Objects.equals(photo, that.photo) && Objects.equals(presentation, that.presentation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, idPromoter, photo, presentation);
    }
}
