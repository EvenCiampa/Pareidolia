package com.pareidolia.state;

import com.pareidolia.entity.Event;

public class PublishedState extends State {

	public PublishedState(Event event) {
		super(event);
	}

	@Override
	public String getStateName() {
		return Event.EventState.PUBLISHED.name();
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
}
