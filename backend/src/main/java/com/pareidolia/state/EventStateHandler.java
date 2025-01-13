package com.pareidolia.state;

import com.pareidolia.entity.Event;
import org.springframework.stereotype.Component;

public final class EventStateHandler {
	public static void moveTo(Event event, Event.EventState state) {
		switch (state) {
			case DRAFT:
				moveToDraft(event);
				return;
			case REVIEW:
				moveToReview(event);
				return;
			case PUBLISHED:
				publish(event);
				return;
			default:
				throw new IllegalStateException(state + " state not managed");
		}
	}

	public static void moveToReview(Event event) {
		if (event.getState() != Event.EventState.DRAFT) {
			throw new IllegalStateException("Can only move to review from DRAFT state");
		}
		event.setState(Event.EventState.REVIEW);
	}

	public static void moveToDraft(Event event) {
		if (event.getState() != Event.EventState.REVIEW) {
			throw new IllegalStateException("Can only move to draft from REVIEW state");
		}
		event.setState(Event.EventState.DRAFT);
	}

	public static void publish(Event event) {
		if (event.getState() != Event.EventState.REVIEW) {
			throw new IllegalStateException("Can only publish from REVIEW state");
		}
		event.setState(Event.EventState.PUBLISHED);
	}

	public static boolean canEdit(Event event) {
		return event.getState() == Event.EventState.DRAFT;
	}

	public static boolean canSendMessage(Event event) {
		return event.getState() == Event.EventState.DRAFT
			|| event.getState() == Event.EventState.REVIEW;
	}
} 