package com.pareidolia.state;

import com.pareidolia.entity.Event;

import java.util.Objects;

public class ReviewState extends State {
	public static final String name = "REVIEW";

	public ReviewState(Event event) {
		super(event);
	}

	@Override
	public String getStateName() {
		return name;
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

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ReviewState state)) return false;
		return Objects.equals(event, state.event);
	}
}
