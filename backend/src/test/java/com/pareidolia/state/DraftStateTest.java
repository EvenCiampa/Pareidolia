package com.pareidolia.state;

import com.pareidolia.entity.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class DraftStateTest {
	@Mock
	private Event mockEvent;

	private DraftState draftState;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		draftState = new DraftState(mockEvent);
	}

	@Test
	void testGetStateName() {
		assertEquals("DRAFT", draftState.getStateName());
	}

	@Test
	void testMoveForward() {
		draftState.moveForward();
		verify(mockEvent).setState(new ReviewState(mockEvent));
	}

	@Test
	void testMoveBackwards() {
		assertThrows(IllegalStateException.class,
			() -> draftState.moveBackwards(),
			"Cannot be moved to a backwards status");
	}

	@Test
	void testCanEdit() {
		assertTrue(draftState.canEdit());
	}

	@Test
	void testEquals() {
		DraftState state1 = new DraftState(mockEvent);
		DraftState state2 = new DraftState(mockEvent);
		ReviewState state3 = new ReviewState(mockEvent);

		assertEquals(state1, state2);
		assertNotEquals(state1, state3);
	}

	@Test
	void testHashCode() {
		DraftState state1 = new DraftState(mockEvent);
		DraftState state2 = new DraftState(mockEvent);

		assertEquals(state1.hashCode(), state2.hashCode());
	}
} 