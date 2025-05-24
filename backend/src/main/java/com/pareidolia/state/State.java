package com.pareidolia.state;

import com.pareidolia.entity.Event;
import lombok.EqualsAndHashCode;

/**
 * Interfaccia comune per tutti gli stati.
 */
@EqualsAndHashCode
public abstract class State {
	Event event;

	public State(Event event) {
		this.event = event;
	}

	public static State fromString(String state, Event event) {
		return switch (state) {
			case DraftState.name -> new DraftState(event);
			case ReviewState.name -> new ReviewState(event);
			case PublishedState.name -> new PublishedState(event);
			default -> throw new IllegalArgumentException("Stato sconosciuto: " + state);
		};
	}

	public abstract String getStateName();

	public abstract void moveForward();

	public abstract void moveBackwards();

	public abstract boolean canEdit();
}
