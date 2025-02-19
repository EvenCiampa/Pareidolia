package com.pareidolia.state;

import com.pareidolia.entity.Event;

public class DraftState extends State {

	public DraftState(Event event) {
		super(event);
	}

	@Override
	public String getStateName() {
		return Event.EventState.DRAFT.name();
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
}
