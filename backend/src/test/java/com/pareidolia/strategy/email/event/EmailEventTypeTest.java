package com.pareidolia.strategy.email.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailEventTypeTest {

	@Test
	void testEnumValues() {
		EmailEventType[] types = EmailEventType.values();
		assertEquals(2, types.length);
		assertEquals(EmailEventType.CREATED, types[0]);
		assertEquals(EmailEventType.UPDATED, types[1]);
	}

	@Test
	void testEnumValueOf() {
		assertEquals(EmailEventType.CREATED, EmailEventType.valueOf("CREATED"));
		assertEquals(EmailEventType.UPDATED, EmailEventType.valueOf("UPDATED"));
	}

	@Test
	void testEnumToString() {
		assertEquals("CREATED", EmailEventType.CREATED.toString());
		assertEquals("UPDATED", EmailEventType.UPDATED.toString());
	}
} 