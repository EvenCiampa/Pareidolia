package com.pareidolia.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReviewTest {

	@Test
	void testNoArgsConstructor() {
		Review review = new Review();
		assertNotNull(review);
	}

	@Test
	void testAllArgsConstructor() {
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account();
		Event event = new Event();

		Review review = new Review(1L, "Great Event", "Amazing experience",
			5L, 2L, 3L, "POSITIVE", false, now, now, account, event);

		assertEquals(1L, review.getId());
		assertEquals("Great Event", review.getTitle());
		assertEquals("Amazing experience", review.getDescription());
		assertEquals(5L, review.getScore());
		assertEquals(2L, review.getIdConsumer());
		assertEquals(3L, review.getIdEvent());
		assertEquals("POSITIVE", review.getTag());
		assertFalse(review.isAnonymous());
		assertEquals(now, review.getCreationTime());
		assertEquals(now, review.getLastUpdate());
		assertEquals(account, review.getAccount());
		assertEquals(event, review.getEvent());
	}

	@Test
	void testBuilder() {
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account();
		Event event = new Event();

		Review review = Review.builder()
			.id(1L)
			.title("Great Event")
			.description("Amazing experience")
			.score(5L)
			.idConsumer(2L)
			.idEvent(3L)
			.tag("POSITIVE")
			.isAnonymous(false)
			.creationTime(now)
			.lastUpdate(now)
			.account(account)
			.event(event)
			.build();

		assertEquals(1L, review.getId());
		assertEquals("Great Event", review.getTitle());
		assertEquals("Amazing experience", review.getDescription());
		assertEquals(5L, review.getScore());
		assertEquals(2L, review.getIdConsumer());
		assertEquals(3L, review.getIdEvent());
		assertEquals("POSITIVE", review.getTag());
		assertFalse(review.isAnonymous());
		assertEquals(now, review.getCreationTime());
		assertEquals(now, review.getLastUpdate());
		assertEquals(account, review.getAccount());
		assertEquals(event, review.getEvent());
	}

	@Test
	void testSetters() {
		Review review = new Review();
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account();
		Event event = new Event();

		review.setId(1L);
		review.setTitle("Great Event");
		review.setDescription("Amazing experience");
		review.setScore(5L);
		review.setIdConsumer(2L);
		review.setIdEvent(3L);
		review.setTag("POSITIVE");
		review.setAnonymous(false);
		review.setCreationTime(now);
		review.setLastUpdate(now);
		review.setAccount(account);
		review.setEvent(event);

		assertEquals(1L, review.getId());
		assertEquals("Great Event", review.getTitle());
		assertEquals("Amazing experience", review.getDescription());
		assertEquals(5L, review.getScore());
		assertEquals(2L, review.getIdConsumer());
		assertEquals(3L, review.getIdEvent());
		assertEquals("POSITIVE", review.getTag());
		assertFalse(review.isAnonymous());
		assertEquals(now, review.getCreationTime());
		assertEquals(now, review.getLastUpdate());
		assertEquals(account, review.getAccount());
		assertEquals(event, review.getEvent());
	}

	@Test
	void testEqualsAndHashCode() {
		LocalDateTime now = LocalDateTime.now();
		Account account1 = new Account();
		Event event1 = new Event();
		Account account2 = new Account();
		Event event2 = new Event();

		Review review1 = Review.builder()
			.id(1L)
			.title("Great Event")
			.description("Amazing experience")
			.score(5L)
			.idConsumer(2L)
			.idEvent(3L)
			.tag("POSITIVE")
			.isAnonymous(false)
			.creationTime(now)
			.lastUpdate(now)
			.account(account1)
			.event(event1)
			.build();

		Review review2 = Review.builder()
			.id(1L)
			.title("Great Event")
			.description("Amazing experience")
			.score(5L)
			.idConsumer(2L)
			.idEvent(3L)
			.tag("POSITIVE")
			.isAnonymous(false)
			.creationTime(now)
			.lastUpdate(now)
			.account(account2)
			.event(event2)
			.build();

		// Should be equal even with different account and event objects due to @EqualsAndHashCode(exclude = {"account", "event"})
		assertEquals(review1, review2);
		assertEquals(review1.hashCode(), review2.hashCode());

		// Test with different values
		Review review3 = Review.builder()
			.id(2L)
			.title("Different Event")
			.build();

		assertNotEquals(review1, review3);
		assertNotEquals(review1.hashCode(), review3.hashCode());
	}
} 