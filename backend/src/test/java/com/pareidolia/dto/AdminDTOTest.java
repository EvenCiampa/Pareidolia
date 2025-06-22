package com.pareidolia.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminDTOTest extends AccountDTOTest {

	@Test
	void testAdminDTOConstructor() {
		AdminDTO dto = new AdminDTO(TEST_ID, TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_EMAIL, TEST_REFERENCE_TYPE, TEST_CREATION_TIME);

		assertEquals(TEST_ID, dto.getId());
		assertEquals(TEST_NAME, dto.getName());
		assertEquals(TEST_SURNAME, dto.getSurname());
		assertEquals(TEST_PHONE, dto.getPhone());
		assertEquals(TEST_EMAIL, dto.getEmail());
		assertEquals(TEST_REFERENCE_TYPE, dto.getReferenceType());
		assertEquals(TEST_CREATION_TIME, dto.getCreationTime());
	}

	@Test
	void testAdminDTOInheritance() {
		AdminDTO dto = new AdminDTO();
		assertInstanceOf(AccountDTO.class, dto);
	}

	@Test
	void testAdminDTOEqualsAndHashCode() {
		AdminDTO dto1 = new AdminDTO(TEST_ID, TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_EMAIL, TEST_REFERENCE_TYPE, TEST_CREATION_TIME);
		AdminDTO dto2 = new AdminDTO(TEST_ID, TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_EMAIL, TEST_REFERENCE_TYPE, TEST_CREATION_TIME);
		AdminDTO dto3 = new AdminDTO(2L, TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_EMAIL, TEST_REFERENCE_TYPE, TEST_CREATION_TIME);

		assertEquals(dto1, dto2);
		assertEquals(dto1.hashCode(), dto2.hashCode());
		assertNotEquals(dto1, dto3);
		assertNotEquals(dto1.hashCode(), dto3.hashCode());
	}

	@Test
	void testAdminDTOToString() {
		AdminDTO dto = new AdminDTO(TEST_ID, TEST_NAME, TEST_SURNAME, TEST_PHONE, TEST_EMAIL, TEST_REFERENCE_TYPE, TEST_CREATION_TIME);
		String toString = dto.toString();

		assertTrue(toString.contains(TEST_ID.toString()));
		assertTrue(toString.contains(TEST_NAME));
		assertTrue(toString.contains(TEST_SURNAME));
		assertTrue(toString.contains(TEST_PHONE));
		assertTrue(toString.contains(TEST_EMAIL));
		assertTrue(toString.contains(TEST_REFERENCE_TYPE));
		assertTrue(toString.contains("AdminDTO"));
	}
} 