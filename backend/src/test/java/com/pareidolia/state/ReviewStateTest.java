package com.pareidolia.state;

import com.pareidolia.entity.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class ReviewStateTest {
	@Mock
	private Event mockEvent;

	private ReviewState reviewState;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		reviewState = new ReviewState(mockEvent);
	}

	@Test
	void testGetStateName() {
		assertEquals("REVIEW", reviewState.getStateName());
	}

	@Test
	void testMoveForward() {
		reviewState.moveForward();
		verify(mockEvent).setState(new PublishedState(mockEvent));
	}

	@Test
	void testMoveBackwards() {
		reviewState.moveBackwards();
		verify(mockEvent).setState(new DraftState(mockEvent));
	}

	@Test
	void testCanEdit() {
		assertFalse(reviewState.canEdit());
	}

	@Test
	void testEquals() {
		ReviewState state1 = new ReviewState(mockEvent);
		ReviewState state2 = new ReviewState(mockEvent);
		DraftState state3 = new DraftState(mockEvent);

		assertEquals(state1, state2);
		assertNotEquals(state1, state3);
	}

	@Test
	void testHashCode() {
		ReviewState state1 = new ReviewState(mockEvent);
		ReviewState state2 = new ReviewState(mockEvent);

		assertEquals(state1.hashCode(), state2.hashCode());
	}
} 