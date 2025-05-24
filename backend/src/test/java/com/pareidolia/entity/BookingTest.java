package com.pareidolia.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingTest {

	@Test
	void testNoArgsConstructor() {
		Booking booking = new Booking();
		assertNotNull(booking);
	}

	@Test
	void testAllArgsConstructor() {
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account();
		Event event = new Event();

		Booking booking = new Booking(1L, 2L, 3L, now, now, account, event);

		assertEquals(1L, booking.getId());
		assertEquals(2L, booking.getIdEvent());
		assertEquals(3L, booking.getIdAccount());
		assertEquals(now, booking.getCreationTime());
		assertEquals(now, booking.getLastUpdate());
		assertEquals(account, booking.getAccount());
		assertEquals(event, booking.getEvent());
	}

	@Test
	void testBuilder() {
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account();
		Event event = new Event();

		Booking booking = Booking.builder()
			.id(1L)
			.idEvent(2L)
			.idAccount(3L)
			.creationTime(now)
			.lastUpdate(now)
			.account(account)
			.event(event)
			.build();

		assertEquals(1L, booking.getId());
		assertEquals(2L, booking.getIdEvent());
		assertEquals(3L, booking.getIdAccount());
		assertEquals(now, booking.getCreationTime());
		assertEquals(now, booking.getLastUpdate());
		assertEquals(account, booking.getAccount());
		assertEquals(event, booking.getEvent());
	}

	@Test
	void testSetters() {
		Booking booking = new Booking();
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account();
		Event event = new Event();

		booking.setId(1L);
		booking.setIdEvent(2L);
		booking.setIdAccount(3L);
		booking.setCreationTime(now);
		booking.setLastUpdate(now);
		booking.setAccount(account);
		booking.setEvent(event);

		assertEquals(1L, booking.getId());
		assertEquals(2L, booking.getIdEvent());
		assertEquals(3L, booking.getIdAccount());
		assertEquals(now, booking.getCreationTime());
		assertEquals(now, booking.getLastUpdate());
		assertEquals(account, booking.getAccount());
		assertEquals(event, booking.getEvent());
	}

	@Test
	void testEqualsAndHashCode() {
		LocalDateTime now = LocalDateTime.now();
		Account account1 = new Account();
		Event event1 = new Event();
		Account account2 = new Account();
		Event event2 = new Event();

		Booking booking1 = Booking.builder()
			.id(1L)
			.idEvent(2L)
			.idAccount(3L)
			.creationTime(now)
			.lastUpdate(now)
			.account(account1)
			.event(event1)
			.build();

		Booking booking2 = Booking.builder()
			.id(1L)
			.idEvent(2L)
			.idAccount(3L)
			.creationTime(now)
			.lastUpdate(now)
			.account(account2)
			.event(event2)
			.build();

		// Should be equal even with different account and event objects due to @EqualsAndHashCode(exclude = {"account", "event"})
		assertEquals(booking1, booking2);
		assertEquals(booking1.hashCode(), booking2.hashCode());

		// Test with different values
		Booking booking3 = Booking.builder()
			.id(2L)
			.idEvent(3L)
			.idAccount(4L)
			.build();

		assertNotEquals(booking1, booking3);
		assertNotEquals(booking1.hashCode(), booking3.hashCode());
	}
} 