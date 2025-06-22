package com.pareidolia.repository.model;

import com.pareidolia.entity.Event;
import jakarta.persistence.EntityResult;
import jakarta.persistence.SqlResultSetMapping;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;
import java.util.Optional;

@Getter
@Setter
@ToString
@Immutable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@SqlResultSetMapping(name = "EventWithInfoForAccount", entities = {@EntityResult(entityClass = EventWithInfoForAccount.class)})
public class EventWithInfoForAccount implements Serializable {
	private Event event;
	private Boolean booked;
	private Long currentParticipants;

	@SuppressWarnings("unused")
	public EventWithInfoForAccount(Event event, Boolean booked, Integer currentParticipants) {
		this.event = event;
		this.booked = booked;
		this.currentParticipants = Optional.ofNullable(currentParticipants).map(Integer::longValue).orElse(null);
	}
}
