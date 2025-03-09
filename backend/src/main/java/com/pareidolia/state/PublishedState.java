package com.pareidolia.state;

import com.pareidolia.entity.Event;

import java.util.Objects;

public class PublishedState extends State {
	public static final String name = "PUBLISHED";

	public PublishedState(Event event) {
		super(event);
	}

	@Override
	public String getStateName() {
		return name;
	}

	@Override
	public void moveForward() {
		throw new IllegalStateException("Cannot be moved to a backwards status");
	}

	@Override
	public void moveBackwards() {
		event.setState(new DraftState(event));
	}

	@Override
	public boolean canEdit() {
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PublishedState state)) return false;
		return Objects.equals(event, state.event);
	}
}
