package com.pareidolia.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "EventDraft", indexes = {
        @Index(name = "eventDraft_id_utente", columnList = "id_account"),
        @Index(name = "eventDraft_data_ora", columnList = "date,time")
})
// TODO join between EventDraft -- 1 to many --> EventDraftPromoterAssociation
// TODO join between EventDraft -- 1 to one --> Event
public class EventDraft {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Long id;
    @Basic
    @Column(name = "id_event")
    private Long idEvent;
    @Basic
    @Column(name = "title", nullable = false)
    private String title;
    @Basic
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Basic
    @Column(name = "place")
    private String place;
    @Basic
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @Basic
    @Column(name = "time", nullable = false)
    private LocalTime time;
    @Basic
    @Column(name = "duration", nullable = false)
    private Duration duration;
    @Basic
    @Column(name = "max_number_of_participants", nullable = false)
    private Long maxNumberOfParticipants;
    @CreationTimestamp
    @Column(name = "creation_time", nullable = false, updatable = false)
    private LocalDateTime creationTime;
    @UpdateTimestamp
    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventDraft that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(description, that.description) && Objects.equals(place, that.place) && Objects.equals(date, that.date) && Objects.equals(time, that.time) && Objects.equals(duration, that.duration) && Objects.equals(maxNumberOfParticipants, that.maxNumberOfParticipants);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, place, date, time, duration, maxNumberOfParticipants);
    }
}