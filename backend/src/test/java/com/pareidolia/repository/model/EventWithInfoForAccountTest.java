package com.pareidolia.repository.model;

import com.pareidolia.entity.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventWithInfoForAccountTest {

	@Test
	void testNoArgsConstructor() {
		EventWithInfoForAccount eventWithInfo = new EventWithInfoForAccount();
		assertNull(eventWithInfo.getEvent());
		assertNull(eventWithInfo.getBooked());
		assertNull(eventWithInfo.getCurrentParticipants());
	}

	@Test
	void testAllArgsConstructor() {
		Event event = new Event();
		Boolean booked = true;
		Long participants = 10L;

		EventWithInfoForAccount eventWithInfo = new EventWithInfoForAccount(event, booked, participants);

		assertEquals(event, eventWithInfo.getEvent());
		assertEquals(booked, eventWithInfo.getBooked());
		assertEquals(participants, eventWithInfo.getCurrentParticipants());
	}

	@Test
	void testConstructorWithInteger() {
		Event event = new Event();
		Boolean booked = true;
		Integer participants = 10;

		EventWithInfoForAccount eventWithInfo = new EventWithInfoForAccount(event, booked, participants);

		assertEquals(event, eventWithInfo.getEvent());
		assertEquals(booked, eventWithInfo.getBooked());
		assertEquals(Long.valueOf(participants), eventWithInfo.getCurrentParticipants());
	}

	@Test
	void testConstructorWithNullParticipants() {
		Event event = new Event();
		Boolean booked = true;
		Integer participants = null;

		EventWithInfoForAccount eventWithInfo = new EventWithInfoForAccount(event, booked, participants);

		assertEquals(event, eventWithInfo.getEvent());
		assertEquals(booked, eventWithInfo.getBooked());
		assertNull(eventWithInfo.getCurrentParticipants());
	}

	@Test
	void testSettersAndGetters() {
		EventWithInfoForAccount eventWithInfo = new EventWithInfoForAccount();

		Event event = new Event();
		Boolean booked = true;
		Long participants = 20L;

		eventWithInfo.setEvent(event);
		eventWithInfo.setBooked(booked);
		eventWithInfo.setCurrentParticipants(participants);

		assertEquals(event, eventWithInfo.getEvent());
		assertEquals(booked, eventWithInfo.getBooked());
		assertEquals(participants, eventWithInfo.getCurrentParticipants());
	}

	@Test
	void testEqualsAndHashCode() {
		Event event = new Event();
		Boolean booked = true;
		Long participants = 10L;

		EventWithInfoForAccount eventWithInfo1 = new EventWithInfoForAccount(event, booked, participants);
		EventWithInfoForAccount eventWithInfo2 = new EventWithInfoForAccount(event, booked, participants);
		EventWithInfoForAccount eventWithInfo3 = new EventWithInfoForAccount(new Event(), false, 20L);

		assertEquals(eventWithInfo1, eventWithInfo2);
		assertEquals(eventWithInfo1.hashCode(), eventWithInfo2.hashCode());

		assertNotEquals(eventWithInfo1, eventWithInfo3);
		assertNotEquals(eventWithInfo1.hashCode(), eventWithInfo3.hashCode());
	}

	@Test
	void testToString() {
		Event event = new Event();
		Boolean booked = true;
		Long participants = 10L;

		EventWithInfoForAccount eventWithInfo = new EventWithInfoForAccount(event, booked, participants);
		String toString = eventWithInfo.toString();

		assertTrue(toString.contains("event=" + event));
		assertTrue(toString.contains("booked=" + booked));
		assertTrue(toString.contains("currentParticipants=" + participants));
	}
} 