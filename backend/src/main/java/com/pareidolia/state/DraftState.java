package com.pareidolia.state;

import com.pareidolia.entity.Event;

import java.util.Objects;

public class DraftState extends State {
	public static final String name = "DRAFT";

	public DraftState(Event event) {
		super(event);
	}

	@Override
	public String getStateName() {
		return name;
	}

	@Override
	public void moveForward() {
		event.setState(new ReviewState(event));
	}

	@Override
	public void moveBackwards() {
		throw new IllegalStateException("Cannot be moved to a backwards status");
	}

	@Override
	public boolean canEdit() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof DraftState state)) return false;
		return Objects.equals(event, state.event);
	}
}
