package com.pareidolia.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PromoterInfoTest {

	@Test
	void testNoArgsConstructor() {
		PromoterInfo promoterInfo = new PromoterInfo();
		assertNotNull(promoterInfo);
	}

	@Test
	void testAllArgsConstructor() {
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account();

		PromoterInfo promoterInfo = new PromoterInfo(1L, 2L, "photo.jpg",
			"Professional event promoter", now, now, account);

		assertEquals(1L, promoterInfo.getId());
		assertEquals(2L, promoterInfo.getIdPromoter());
		assertEquals("photo.jpg", promoterInfo.getPhoto());
		assertEquals("Professional event promoter", promoterInfo.getPresentation());
		assertEquals(now, promoterInfo.getCreationTime());
		assertEquals(now, promoterInfo.getLastUpdate());
		assertEquals(account, promoterInfo.getAccount());
	}

	@Test
	void testBuilder() {
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account();

		PromoterInfo promoterInfo = PromoterInfo.builder()
			.id(1L)
			.idPromoter(2L)
			.photo("photo.jpg")
			.presentation("Professional event promoter")
			.creationTime(now)
			.lastUpdate(now)
			.account(account)
			.build();

		assertEquals(1L, promoterInfo.getId());
		assertEquals(2L, promoterInfo.getIdPromoter());
		assertEquals("photo.jpg", promoterInfo.getPhoto());
		assertEquals("Professional event promoter", promoterInfo.getPresentation());
		assertEquals(now, promoterInfo.getCreationTime());
		assertEquals(now, promoterInfo.getLastUpdate());
		assertEquals(account, promoterInfo.getAccount());
	}

	@Test
	void testSetters() {
		PromoterInfo promoterInfo = new PromoterInfo();
		LocalDateTime now = LocalDateTime.now();
		Account account = new Account();

		promoterInfo.setId(1L);
		promoterInfo.setIdPromoter(2L);
		promoterInfo.setPhoto("photo.jpg");
		promoterInfo.setPresentation("Professional event promoter");
		promoterInfo.setCreationTime(now);
		promoterInfo.setLastUpdate(now);
		promoterInfo.setAccount(account);

		assertEquals(1L, promoterInfo.getId());
		assertEquals(2L, promoterInfo.getIdPromoter());
		assertEquals("photo.jpg", promoterInfo.getPhoto());
		assertEquals("Professional event promoter", promoterInfo.getPresentation());
		assertEquals(now, promoterInfo.getCreationTime());
		assertEquals(now, promoterInfo.getLastUpdate());
		assertEquals(account, promoterInfo.getAccount());
	}

	@Test
	void testEqualsAndHashCode() {
		LocalDateTime now = LocalDateTime.now();
		Account account1 = new Account();
		Account account2 = new Account();

		PromoterInfo promoterInfo1 = PromoterInfo.builder()
			.id(1L)
			.idPromoter(2L)
			.photo("photo.jpg")
			.presentation("Professional event promoter")
			.creationTime(now)
			.lastUpdate(now)
			.account(account1)
			.build();

		PromoterInfo promoterInfo2 = PromoterInfo.builder()
			.id(1L)
			.idPromoter(2L)
			.photo("photo.jpg")
			.presentation("Professional event promoter")
			.creationTime(now)
			.lastUpdate(now)
			.account(account2)
			.build();

		// Should be equal even with different account objects due to @EqualsAndHashCode(exclude = {"account"})
		assertEquals(promoterInfo1, promoterInfo2);
		assertEquals(promoterInfo1.hashCode(), promoterInfo2.hashCode());

		// Test with different values
		PromoterInfo promoterInfo3 = PromoterInfo.builder()
			.id(2L)
			.idPromoter(3L)
			.build();

		assertNotEquals(promoterInfo1, promoterInfo3);
		assertNotEquals(promoterInfo1.hashCode(), promoterInfo3.hashCode());
	}

	@Test
	void testLongPresentation() {
		// Test with a presentation that approaches the LOB limit
		StringBuilder longPresentation = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			longPresentation.append("This is a very long presentation that will be stored in a LOB column. ");
		}

		PromoterInfo promoterInfo = new PromoterInfo();
		promoterInfo.setPresentation(longPresentation.toString());

		assertEquals(longPresentation.toString(), promoterInfo.getPresentation());
	}
} 