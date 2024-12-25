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
@Table(name = "Message")
public class Message {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    @Basic
    @Column(name = "id_account", nullable = false)
    private Long idAccount;
    @Basic
    @Column(name = "id_event_draft", nullable = false)
    private Long idEventDraft;
	@CreationTimestamp
	@Column(name = "creation_time", nullable = false, updatable = false)
	private LocalDateTime creationTime;
	@UpdateTimestamp
	@Column(name = "last_update", nullable = false)
	private LocalDateTime lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message message1)) return false;
        return Objects.equals(id, message1.id) && Objects.equals(message, message1.message) && Objects.equals(idAccount, message1.idAccount) && Objects.equals(idEventDraft, message1.idEventDraft);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, idAccount, idEventDraft);
    }
}