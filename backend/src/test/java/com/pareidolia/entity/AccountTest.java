package com.pareidolia.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

	@Test
	void testNoArgsConstructor() {
		Account account = new Account();
		assertNotNull(account);
	}

	@Test
	void testCustomConstructor() {
		Account account = new Account(
			"John", "Doe", "john@example.com", "password123",
			"+1234567890", Account.Type.CONSUMER);

		assertEquals("John", account.getName());
		assertEquals("Doe", account.getSurname());
		assertEquals("john@example.com", account.getEmail());
		assertEquals("password123", account.getPassword());
		assertEquals("+1234567890", account.getPhone());
		assertEquals(Account.Type.CONSUMER, account.getReferenceType());
	}

	@Test
	void testAllArgsConstructor() {
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account(1L, "john@example.com", "password123",
			"John", "Doe", "+1234567890", Account.Type.CONSUMER,
			now, now);

		assertEquals(1L, account.getId());
		assertEquals("john@example.com", account.getEmail());
		assertEquals("password123", account.getPassword());
		assertEquals("John", account.getName());
		assertEquals("Doe", account.getSurname());
		assertEquals("+1234567890", account.getPhone());
		assertEquals(Account.Type.CONSUMER, account.getReferenceType());
		assertEquals(now, account.getCreationTime());
		assertEquals(now, account.getLastUpdate());
	}

	@Test
	void testBuilder() {
		LocalDateTime now = LocalDateTime.now();
		Account account = Account.builder()
			.id(1L)
			.email("john@example.com")
			.password("password123")
			.name("John")
			.surname("Doe")
			.phone("+1234567890")
			.referenceType(Account.Type.CONSUMER)
			.creationTime(now)
			.lastUpdate(now)
			.build();

		assertEquals(1L, account.getId());
		assertEquals("john@example.com", account.getEmail());
		assertEquals("password123", account.getPassword());
		assertEquals("John", account.getName());
		assertEquals("Doe", account.getSurname());
		assertEquals("+1234567890", account.getPhone());
		assertEquals(Account.Type.CONSUMER, account.getReferenceType());
		assertEquals(now, account.getCreationTime());
		assertEquals(now, account.getLastUpdate());
	}

	@Test
	void testSetters() {
		Account account = new Account();
		LocalDateTime now = LocalDateTime.now();

		account.setId(1L);
		account.setEmail("john@example.com");
		account.setPassword("password123");
		account.setName("John");
		account.setSurname("Doe");
		account.setPhone("+1234567890");
		account.setReferenceType(Account.Type.CONSUMER);
		account.setCreationTime(now);
		account.setLastUpdate(now);

		assertEquals(1L, account.getId());
		assertEquals("john@example.com", account.getEmail());
		assertEquals("password123", account.getPassword());
		assertEquals("John", account.getName());
		assertEquals("Doe", account.getSurname());
		assertEquals("+1234567890", account.getPhone());
		assertEquals(Account.Type.CONSUMER, account.getReferenceType());
		assertEquals(now, account.getCreationTime());
		assertEquals(now, account.getLastUpdate());
	}

	@Test
	void testEqualsAndHashCode() {
		LocalDateTime now = LocalDateTime.now();
		Account account1 = Account.builder()
			.id(1L)
			.email("john@example.com")
			.password("password123")
			.name("John")
			.surname("Doe")
			.phone("+1234567890")
			.referenceType(Account.Type.CONSUMER)
			.creationTime(now)
			.lastUpdate(now)
			.build();

		Account account2 = Account.builder()
			.id(1L)
			.email("john@example.com")
			.password("password123")
			.name("John")
			.surname("Doe")
			.phone("+1234567890")
			.referenceType(Account.Type.CONSUMER)
			.creationTime(now)
			.lastUpdate(now)
			.build();

		assertEquals(account1, account2);
		assertEquals(account1.hashCode(), account2.hashCode());

		// Test with different values
		Account account3 = Account.builder()
			.id(2L)
			.email("jane@example.com")
			.build();

		assertNotEquals(account1, account3);
		assertNotEquals(account1.hashCode(), account3.hashCode());
	}

	@Test
	void testAccountTypeEnum() {
		// Test all enum values
		assertEquals(4, Account.Type.values().length);
		assertEquals(Account.Type.CONSUMER, Account.Type.valueOf("CONSUMER"));
		assertEquals(Account.Type.PROMOTER, Account.Type.valueOf("PROMOTER"));
		assertEquals(Account.Type.REVIEWER, Account.Type.valueOf("REVIEWER"));
		assertEquals(Account.Type.ADMIN, Account.Type.valueOf("ADMIN"));
	}
} 