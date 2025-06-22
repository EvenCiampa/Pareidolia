package com.pareidolia.decorator.review;

import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class HighlightDecoratorTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private ReviewComponent decoratedReview;

	@Mock
	private Account mockAccount;

	private HighlightDecorator highlightDecorator;
	private ReviewDTO baseReviewDTO;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		baseReviewDTO = new ReviewDTO();
		baseReviewDTO.setIdConsumer(1L);
		baseReviewDTO.setAccountName("John Doe");
		when(decoratedReview.apply()).thenReturn(baseReviewDTO);
		highlightDecorator = new HighlightDecorator(decoratedReview, accountRepository);
	}

	@Test
	void testApply_WithPromoterAccount() {
		// Setup
		when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));
		when(mockAccount.getReferenceType()).thenReturn(Account.Type.PROMOTER);

		// Test
		ReviewDTO result = highlightDecorator.apply();

		// Verify
		assertNotNull(result);
		assertEquals("⭐️ [Promoter] John Doe", result.getAccountName());
	}

	@Test
	void testApply_WithNonPromoterAccount() {
		// Setup
		when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));
		when(mockAccount.getReferenceType()).thenReturn(Account.Type.CONSUMER);

		// Test
		ReviewDTO result = highlightDecorator.apply();

		// Verify
		assertNotNull(result);
		assertEquals("John Doe", result.getAccountName());
	}

	@Test
	void testApply_WithAccountNotFound() {
		// Setup
		when(accountRepository.findById(any())).thenReturn(Optional.empty());

		// Test
		ReviewDTO result = highlightDecorator.apply();

		// Verify
		assertNotNull(result);
		assertEquals("John Doe", result.getAccountName());
	}

	@Test
	void testApply_WithException() {
		// Setup
		when(accountRepository.findById(any())).thenThrow(new RuntimeException("Test exception"));

		// Act & Assert
		RuntimeException exception = assertThrows(RuntimeException.class, () ->
			highlightDecorator.apply()
		);
		assertEquals("Test exception", exception.getMessage());
	}
} 