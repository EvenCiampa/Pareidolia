package com.pareidolia.state;

import com.pareidolia.entity.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class PublishedStateTest {
	@Mock
	private Event mockEvent;

	private PublishedState publishedState;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		publishedState = new PublishedState(mockEvent);
	}

	@Test
	void testGetStateName() {
		assertEquals("PUBLISHED", publishedState.getStateName());
	}

	@Test
	void testMoveForward() {
		assertThrows(IllegalStateException.class,
			() -> publishedState.moveForward(),
			"Cannot be moved to a forward status");
	}

	@Test
	void testMoveBackwards() {
		publishedState.moveBackwards();
		verify(mockEvent).setState(new DraftState(mockEvent));
	}

	@Test
	void testCanEdit() {
		assertFalse(publishedState.canEdit());
	}

	@Test
	void testEquals() {
		PublishedState state1 = new PublishedState(mockEvent);
		PublishedState state2 = new PublishedState(mockEvent);
		ReviewState state3 = new ReviewState(mockEvent);

		assertEquals(state1, state2);
		assertNotEquals(state1, state3);
	}

	@Test
	void testHashCode() {
		PublishedState state1 = new PublishedState(mockEvent);
		PublishedState state2 = new PublishedState(mockEvent);

		assertEquals(state1.hashCode(), state2.hashCode());
	}
} 