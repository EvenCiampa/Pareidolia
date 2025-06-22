package com.pareidolia.state;

import com.pareidolia.entity.Event;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
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
}
