package com.pareidolia.state;

import com.pareidolia.entity.Event;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
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
		throw new IllegalStateException("Cannot be moved to a forward status");
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
