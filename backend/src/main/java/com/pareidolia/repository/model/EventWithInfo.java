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
@SqlResultSetMapping(name = "EventWithInfo", entities = {@EntityResult(entityClass = EventWithInfo.class)})
public class EventWithInfo implements Serializable {
	private Event event;
	private Long currentParticipants;

	@SuppressWarnings("unused")
	public EventWithInfo(Event event, Integer currentParticipants) {
		this.event = event;
		this.currentParticipants = Optional.ofNullable(currentParticipants).map(Integer::longValue).orElse(null);
	}
}
