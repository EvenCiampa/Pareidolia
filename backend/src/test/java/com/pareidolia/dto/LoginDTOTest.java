package com.pareidolia.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginDTOTest {

	@Test
	void testConstructorAndGetters() {
		LoginDTO dto = new LoginDTO();
		assertNotNull(dto);

		dto = new LoginDTO("test@example.com", "password123");

		assertEquals("test@example.com", dto.getEmail());
		assertEquals("password123", dto.getPassword());
	}

	@Test
	void testSetters() {
		LoginDTO dto = new LoginDTO();

		dto.setEmail("test@example.com");
		dto.setPassword("password123");

		assertEquals("test@example.com", dto.getEmail());
		assertEquals("password123", dto.getPassword());
	}

	@Test
	void testEqualsAndHashCode() {
		LoginDTO dto1 = new LoginDTO("test@example.com", "password123");
		LoginDTO dto2 = new LoginDTO("test@example.com", "password123");

		assertEquals(dto1, dto2);
		assertEquals(dto1.hashCode(), dto2.hashCode());

		// Test with different values
		LoginDTO dto3 = new LoginDTO("other@example.com", "password123");
		assertNotEquals(dto1, dto3);
		assertNotEquals(dto1.hashCode(), dto3.hashCode());
	}

	@Test
	void testToString() {
		LoginDTO dto = new LoginDTO("test@example.com", "password123");

		String toString = dto.toString();
		assertNotNull(toString);
		assertTrue(toString.contains("email=test@example.com"));
		// Password should be excluded from toString due to @ToString.Exclude
		assertFalse(toString.contains("password123"));
	}
} 