package com.pareidolia.state;

import com.pareidolia.entity.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class StateTest {
	@Mock
	private Event mockEvent;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testFromString_DraftState() {
		State state = State.fromString(DraftState.name, mockEvent);
		assertInstanceOf(DraftState.class, state);
		assertEquals(DraftState.name, state.getStateName());
	}

	@Test
	void testFromString_ReviewState() {
		State state = State.fromString(ReviewState.name, mockEvent);
		assertInstanceOf(ReviewState.class, state);
		assertEquals(ReviewState.name, state.getStateName());
	}

	@Test
	void testFromString_PublishedState() {
		State state = State.fromString(PublishedState.name, mockEvent);
		assertInstanceOf(PublishedState.class, state);
		assertEquals(PublishedState.name, state.getStateName());
	}

	@Test
	void testFromString_InvalidState() {
		assertThrows(IllegalArgumentException.class,
			() -> State.fromString("INVALID_STATE", mockEvent));
	}

	@Test
	void testEquals() {
		State state1 = new DraftState(mockEvent);
		State state2 = new DraftState(mockEvent);
		State state3 = new ReviewState(mockEvent);

		assertEquals(state1, state2);
		assertNotEquals(state1, state3);
	}

	@Test
	void testHashCode() {
		State state1 = new DraftState(mockEvent);
		State state2 = new DraftState(mockEvent);

		assertEquals(state1.hashCode(), state2.hashCode());
	}
} 