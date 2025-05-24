package com.pareidolia.decorator.review;

import com.pareidolia.dto.ReviewDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class TaggedReviewTest {

	@Mock
	private ReviewComponent decoratedReview;

	private TaggedReview taggedReview;
	private ReviewDTO baseReviewDTO;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		baseReviewDTO = new ReviewDTO();
		baseReviewDTO.setDescription("Test description");
		when(decoratedReview.apply()).thenReturn(baseReviewDTO);
		taggedReview = new TaggedReview(decoratedReview);
	}

	@Test
	void testApply_WithTag() {
		// Setup
		baseReviewDTO.setTag("TestTag");

		// Test
		ReviewDTO result = taggedReview.apply();

		// Verify
		assertNotNull(result);
		assertEquals("Test description [Tag: TestTag]", result.getDescription());
	}

	@Test
	void testApply_WithoutTag() {
		// Setup
		baseReviewDTO.setTag(null);

		// Test
		ReviewDTO result = taggedReview.apply();

		// Verify
		assertNotNull(result);
		assertEquals("Test description", result.getDescription());
	}

	@Test
	void testApply_WithEmptyTag() {
		// Setup
		baseReviewDTO.setTag("");

		// Test
		ReviewDTO result = taggedReview.apply();

		// Verify
		assertNotNull(result);
		assertEquals("Test description", result.getDescription());
	}
} 