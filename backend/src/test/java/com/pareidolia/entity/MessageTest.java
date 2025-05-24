package com.pareidolia.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

	@Test
	void testNoArgsConstructor() {
		Message message = new Message();
		assertNotNull(message);
	}

	@Test
	void testAllArgsConstructor() {
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account();
		Event event = new Event();

		Message message = new Message(1L, "Hello World!", 2L, 3L, now, now, account, event);

		assertEquals(1L, message.getId());
		assertEquals("Hello World!", message.getMessage());
		assertEquals(2L, message.getIdAccount());
		assertEquals(3L, message.getIdEvent());
		assertEquals(now, message.getCreationTime());
		assertEquals(now, message.getLastUpdate());
		assertEquals(account, message.getAccount());
		assertEquals(event, message.getEvent());
	}

	@Test
	void testSetters() {
		Message message = new Message();
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account();
		Event event = new Event();

		message.setId(1L);
		message.setMessage("Hello World!");
		message.setIdAccount(2L);
		message.setIdEvent(3L);
		message.setCreationTime(now);
		message.setLastUpdate(now);
		message.setAccount(account);
		message.setEvent(event);

		assertEquals(1L, message.getId());
		assertEquals("Hello World!", message.getMessage());
		assertEquals(2L, message.getIdAccount());
		assertEquals(3L, message.getIdEvent());
		assertEquals(now, message.getCreationTime());
		assertEquals(now, message.getLastUpdate());
		assertEquals(account, message.getAccount());
		assertEquals(event, message.getEvent());
	}

	@Test
	void testEqualsAndHashCode() {
		LocalDateTime now = LocalDateTime.now();
		Account account1 = new Account();
		Event event1 = new Event();
		Account account2 = new Account();
		Event event2 = new Event();

		Message message1 = new Message(1L, "Hello World!", 2L, 3L, now, now, account1, event1);
		Message message2 = new Message(1L, "Hello World!", 2L, 3L, now, now, account2, event2);

		// Should be equal even with different account and event objects due to @EqualsAndHashCode(exclude = {"account", "event"})
		assertEquals(message1, message2);
		assertEquals(message1.hashCode(), message2.hashCode());

		// Test with different values
		Message message3 = new Message();
		message3.setId(2L);
		message3.setMessage("Different message");

		assertNotEquals(message1, message3);
		assertNotEquals(message1.hashCode(), message3.hashCode());
	}

	@Test
	void testLongMessage() {
		// Test with a message that approaches the LOB limit
		StringBuilder longMessage = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			longMessage.append("This is a very long message that will be stored in a LOB column. ");
		}

		Message message = new Message();
		message.setMessage(longMessage.toString());

		assertEquals(longMessage.toString(), message.getMessage());
	}
} 