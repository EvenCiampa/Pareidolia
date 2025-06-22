package com.pareidolia.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountDTOTest {
	protected static final Long TEST_ID = 1L;
	protected static final String TEST_NAME = "John";
	protected static final String TEST_SURNAME = "Doe";
	protected static final String TEST_PHONE = "1234567890";
	protected static final String TEST_EMAIL = "john.doe@example.com";
	protected static final String TEST_REFERENCE_TYPE = "TEST_TYPE";
	protected static final LocalDateTime TEST_CREATION_TIME = LocalDateTime.now();

	@Test
	void testAccountDTOGettersAndSetters() {
		AccountDTO dto = new AccountDTO();

		dto.setId(TEST_ID);
		dto.setName(TEST_NAME);
		dto.setSurname(TEST_SURNAME);
		dto.setPhone(TEST_PHONE);
		dto.setEmail(TEST_EMAIL);
		dto.setReferenceType(TEST_REFERENCE_TYPE);
		dto.setCreationTime(TEST_CREATION_TIME);

		assertEquals(TEST_ID, dto.getId());
		assertEquals(TEST_NAME, dto.getName());
		assertEquals(TEST_SURNAME, dto.getSurname());
		assertEquals(TEST_PHONE, dto.getPhone());
		assertEquals(TEST_EMAIL, dto.getEmail());
		assertEquals(TEST_REFERENCE_TYPE, dto.getReferenceType());
		assertEquals(TEST_CREATION_TIME, dto.getCreationTime());
	}

	@Test
	void testAccountDTOConstructor() {
		AccountDTO dto = new AccountDTO(TEST_ID, TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_EMAIL, TEST_REFERENCE_TYPE, TEST_CREATION_TIME);

		assertEquals(TEST_ID, dto.getId());
		assertEquals(TEST_NAME, dto.getName());
		assertEquals(TEST_SURNAME, dto.getSurname());
		assertEquals(TEST_PHONE, dto.getPhone());
		assertEquals(TEST_EMAIL, dto.getEmail());
		assertEquals(TEST_REFERENCE_TYPE, dto.getReferenceType());
		assertEquals(TEST_CREATION_TIME, dto.getCreationTime());
	}

	@Test
	void testAccountDTOEqualsAndHashCode() {
		AccountDTO dto1 = new AccountDTO(TEST_ID, TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_EMAIL, TEST_REFERENCE_TYPE, TEST_CREATION_TIME);
		AccountDTO dto2 = new AccountDTO(TEST_ID, TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_EMAIL, TEST_REFERENCE_TYPE, TEST_CREATION_TIME);
		AccountDTO dto3 = new AccountDTO(2L, TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_EMAIL, TEST_REFERENCE_TYPE, TEST_CREATION_TIME);

		assertEquals(dto1, dto2);
		assertEquals(dto1.hashCode(), dto2.hashCode());
		assertNotEquals(dto1, dto3);
		assertNotEquals(dto1.hashCode(), dto3.hashCode());
	}

	@Test
	void testAccountDTOToString() {
		AccountDTO dto = new AccountDTO(TEST_ID, TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_EMAIL, TEST_REFERENCE_TYPE, TEST_CREATION_TIME);
		String toString = dto.toString();

		assertTrue(toString.contains(TEST_ID.toString()));
		assertTrue(toString.contains(TEST_NAME));
		assertTrue(toString.contains(TEST_SURNAME));
		assertTrue(toString.contains(TEST_PHONE));
		assertTrue(toString.contains(TEST_EMAIL));
		assertTrue(toString.contains(TEST_REFERENCE_TYPE));
	}
} 