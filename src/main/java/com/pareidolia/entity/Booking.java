package com.pareidolia.entity;

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
@Table(name = "Reservation", indexes = {
	@Index(name = "reservation_id_utente", columnList = "id_account"),
	@Index(name = "reservation_unique_key", columnList = "id_event,id_account", unique = true)
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
	@CreationTimestamp
	@Column(name = "creation_time", nullable = false, updatable = false)
	private LocalDateTime creationTime;
	@UpdateTimestamp
	@Column(name = "last_update", nullable = false)
	private LocalDateTime lastUpdate;

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Booking booking)) return false;
		return Objects.equals(id, booking.id) && Objects.equals(idEvent, booking.idEvent) && Objects.equals(idAccount, booking.idAccount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, idEvent, idAccount);
	}
}
