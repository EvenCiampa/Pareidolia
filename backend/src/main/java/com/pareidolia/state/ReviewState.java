package com.pareidolia.state;

import com.pareidolia.entity.Event;

public class ReviewState extends State {

	public ReviewState(Event event) {
		super(event);
	}

	@Override
	public String getStateName() {
		return Event.EventState.REVIEW.name();
	}

	@Override
	public void moveForward() {
		event.setState(new PublishedState(event));
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
