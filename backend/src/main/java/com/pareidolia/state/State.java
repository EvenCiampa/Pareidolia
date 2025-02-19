package com.pareidolia.state;

import com.pareidolia.entity.Event;

import java.util.Objects;

/**
 * Interfaccia comune per tutti gli stati.
 */
public abstract class State {
    Event event;

    public State(Event event) {
        this.event = event;
    }

    public static State fromString(String state, Event event) {
        Event.EventState stateEnum;
        try {
            stateEnum = Event.EventState.valueOf(state);
        } catch (Exception ignore) {
            throw new IllegalArgumentException("Stato sconosciuto: " + state);
        }

        return switch (stateEnum) {
            case DRAFT -> new DraftState(event);
            case REVIEW -> new ReviewState(event);
            case PUBLISHED -> new PublishedState(event);
        };
    }

    public abstract String getStateName();

    public abstract void moveForward();

    public abstract void moveBackwards();

    public abstract boolean canEdit();

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof State state)) return false;
        return Objects.equals(getStateName(), state.getStateName());
    }
}
