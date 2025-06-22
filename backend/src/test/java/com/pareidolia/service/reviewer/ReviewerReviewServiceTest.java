package com.pareidolia.service.reviewer;

import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.Review;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.ReviewRepository;
import com.pareidolia.validator.ReviewValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewerReviewServiceTest {

	@Mock
	private ReviewValidator reviewValidator;

	@Mock
	private EventRepository eventRepository;

	@Mock
	private ReviewRepository reviewRepository;

	@Mock
	private AccountRepository accountRepository;

	@InjectMocks
	private ReviewerReviewService reviewerReviewService;

	private Event testEvent;
	private Account testAccount;
	private Review testReview;

	@BeforeEach
	void setUp() {
		testAccount = new Account();
		testAccount.setId(1L);
		testAccount.setEmail("test@example.com");
		testAccount.setReferenceType(Account.Type.CONSUMER);

		testEvent = new Event();
		testEvent.setId(1L);
		testEvent.setTitle("Test Event");

		testReview = new Review();
		testReview.setId(1L);
		testReview.setIdEvent(testEvent.getId());
		testReview.setIdConsumer(testAccount.getId());
		testReview.setScore(5L);
		testReview.setTitle("Great event!");
		testReview.setDescription("This was an amazing experience!");
		testReview.setCreationTime(LocalDateTime.now());
		testReview.setAnonymous(false);
		testReview.setAccount(testAccount);
	}

	@Test
	void testGetEventReviewsSuccess() {
		List<Review> reviews = Collections.singletonList(testReview);
		Page<Review> reviewPage = new PageImpl<>(reviews);

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		doNothing().when(reviewValidator).validateEventIsOver(testEvent.getId());
		when(reviewRepository.findAllByIdEvent(eq(testEvent.getId()), any(PageRequest.class)))
			.thenReturn(reviewPage);
		when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

		Page<ReviewDTO> result = reviewerReviewService.getEventReviews(testEvent.getId(), 0, 10);

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
		assertEquals(1, result.getContent().size());
		assertEquals(testReview.getId(), result.getContent().get(0).id);
		assertEquals(testReview.getScore(), result.getContent().get(0).score);
		assertEquals(testReview.getTitle(), result.getContent().get(0).title);
		assertEquals(testReview.getDescription(), result.getContent().get(0).description);
	}

	@Test
	void testGetEventReviewsNullEventId() {
		assertThrows(IllegalArgumentException.class,
			() -> reviewerReviewService.getEventReviews(null, 0, 10));
	}

	@Test
	void testGetEventReviewsInvalidEventId() {
		when(eventRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerReviewService.getEventReviews(999L, 0, 10));
	}

	@Test
	void testGetEventReviewsEventNotOver() {
		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		doThrow(new IllegalArgumentException("Event not over yet"))
			.when(reviewValidator).validateEventIsOver(testEvent.getId());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerReviewService.getEventReviews(testEvent.getId(), 0, 10));
	}

	@Test
	void testGetEventReviewsWithNullPageAndSize() {
		List<Review> reviews = Collections.singletonList(testReview);
		Page<Review> reviewPage = new PageImpl<>(reviews);

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		doNothing().when(reviewValidator).validateEventIsOver(testEvent.getId());
		when(reviewRepository.findAllByIdEvent(eq(testEvent.getId()), any(PageRequest.class)))
			.thenReturn(reviewPage);
		when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

		Page<ReviewDTO> result = reviewerReviewService.getEventReviews(testEvent.getId(), null, null);

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
		assertEquals(1, result.getContent().size());
	}

	@Test
	void testGetEventReviewsWithNegativePageAndSize() {
		List<Review> reviews = Collections.singletonList(testReview);
		Page<Review> reviewPage = new PageImpl<>(reviews);

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		doNothing().when(reviewValidator).validateEventIsOver(testEvent.getId());
		when(reviewRepository.findAllByIdEvent(eq(testEvent.getId()), any(PageRequest.class)))
			.thenReturn(reviewPage);
		when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));

		Page<ReviewDTO> result = reviewerReviewService.getEventReviews(testEvent.getId(), -1, -1);

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
		assertEquals(1, result.getContent().size());
	}

	@Test
	void testGetEventReviewsEmptyPage() {
		Page<Review> emptyPage = new PageImpl<>(Collections.emptyList());

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		doNothing().when(reviewValidator).validateEventIsOver(testEvent.getId());
		when(reviewRepository.findAllByIdEvent(eq(testEvent.getId()), any(PageRequest.class)))
			.thenReturn(emptyPage);

		Page<ReviewDTO> result = reviewerReviewService.getEventReviews(testEvent.getId(), 0, 10);

		assertNotNull(result);
		assertTrue(result.getContent().isEmpty());
	}
} 