package com.pareidolia.service.consumer;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.Review;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.ReviewRepository;
import com.pareidolia.state.DraftState;
import com.pareidolia.state.PublishedState;
import com.pareidolia.state.State;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Transactional
@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ConsumerReviewServiceTest {

	private final String consumerEmail = "consumer@test.com";
	private final String consumerPassword = "TestPassword123#";
	@Autowired
	private ConsumerReviewService consumerReviewService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private ReviewRepository reviewRepository;
	private Account consumerAccount;
	private Event pastEvent;
	private Review testReview;

	@BeforeEach
	void setUp() {
		// Create consumer account
		consumerAccount = accountRepository.save(Account.builder()
			.email(consumerEmail)
			.password(DigestUtils.sha3_256Hex(consumerPassword))
			.name("Test")
			.surname("Consumer")
			.phone("+39123456789")
			.referenceType(Account.Type.CONSUMER)
			.build());

		// Create past event (for review validation)
		Event event = Event.builder()
			.title("Past Event")
			.description("Past Event Description")
			.place("Test Place")
			.date(LocalDate.now().minusDays(7))  // Past event
			.time(LocalTime.of(20, 0))
			.duration(Duration.ofHours(2))
			.maxNumberOfParticipants(100L)
			.state(State.fromString(DraftState.name, null))
			.build();
		pastEvent = eventRepository.save(event);
		pastEvent.setState(State.fromString(DraftState.name, pastEvent));
		pastEvent = eventRepository.save(pastEvent);

		// Create test review
		testReview = Review.builder()
			.title("Test Review")
			.description("Test Review Description")
			.score(5L)
			.idConsumer(consumerAccount.getId())
			.idEvent(pastEvent.getId())
			.account(consumerAccount)
			.build();
		testReview = reviewRepository.save(testReview);
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testGetEventReviews() {
		// Act
		Page<ReviewDTO> reviews = consumerReviewService.getEventReviews(pastEvent.getId(), 0, 10);

		// Assert
		assertNotNull(reviews);
		assertFalse(reviews.isEmpty());
		assertEquals(1, reviews.getTotalElements());
		ReviewDTO reviewDTO = reviews.getContent().get(0);
		assertEquals(testReview.getId(), reviewDTO.getId());
		assertEquals(testReview.getTitle(), reviewDTO.getTitle());
		assertEquals(testReview.getDescription(), reviewDTO.getDescription());
		assertEquals(testReview.getScore(), reviewDTO.getScore());
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testCreate() {
		// Arrange
		Event anotherPastEvent = eventRepository.save(Event.builder()
			.title("Another Past Event")
			.description("Another Past Event Description")
			.place("Test Place")
			.date(LocalDate.now().minusDays(14))
			.time(LocalTime.of(20, 0))
			.duration(Duration.ofHours(2))
			.maxNumberOfParticipants(100L)
			.state(State.fromString(PublishedState.name, null))
			.build());

		pastEvent.setState(State.fromString(PublishedState.name, pastEvent));

		ReviewDTO newReviewDTO = new ReviewDTO();
		newReviewDTO.setTitle("New Review");
		newReviewDTO.setDescription("New Review Description");
		newReviewDTO.setScore(4L);
		newReviewDTO.setIdEvent(anotherPastEvent.getId());

		// Act
		ReviewDTO createdReviewDTO = consumerReviewService.create(newReviewDTO);

		// Assert
		assertNotNull(createdReviewDTO);
		assertNotNull(createdReviewDTO.getId());
		assertEquals(newReviewDTO.getTitle(), createdReviewDTO.getTitle());
		assertEquals(newReviewDTO.getDescription(), createdReviewDTO.getDescription());
		assertEquals(newReviewDTO.getScore(), createdReviewDTO.getScore());
		assertEquals(consumerAccount.getId(), createdReviewDTO.getIdConsumer());
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testGetEventReviewsWithInvalidEventId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			consumerReviewService.getEventReviews(999L, 0, 10)
		);
		assertEquals("Invalid Event ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testCreateReviewForFutureEvent() {
		// Arrange
		Event futureEvent = eventRepository.save(Event.builder()
			.title("Future Event")
			.description("Future Event Description")
			.place("Test Place")
			.date(LocalDate.now().plusDays(7))  // Future event
			.time(LocalTime.of(20, 0))
			.duration(Duration.ofHours(2))
			.maxNumberOfParticipants(100L)
			.state(State.fromString(PublishedState.name, null))
			.build());

		pastEvent.setState(State.fromString(PublishedState.name, pastEvent));

		ReviewDTO invalidReviewDTO = new ReviewDTO();
		invalidReviewDTO.setTitle("Invalid Review");
		invalidReviewDTO.setDescription("Invalid Review Description");
		invalidReviewDTO.setScore(5L);
		invalidReviewDTO.setIdEvent(futureEvent.getId());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			consumerReviewService.create(invalidReviewDTO)
		);
		assertEquals("Event has not finished yet.", exception.getMessage());
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testCreateReviewWithInvalidScore() {
		// Arrange
		ReviewDTO invalidReviewDTO = new ReviewDTO();
		invalidReviewDTO.setTitle("Invalid Review");
		invalidReviewDTO.setDescription("Invalid Review Description");
		invalidReviewDTO.setScore(6L);  // Invalid score > 5
		invalidReviewDTO.setIdEvent(pastEvent.getId());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			consumerReviewService.create(invalidReviewDTO)
		);
		assertEquals("Score must be between 1 and 5.", exception.getMessage());
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testCreateReviewWithEmptyTitle() {
		// Arrange
		ReviewDTO invalidReviewDTO = new ReviewDTO();
		invalidReviewDTO.setTitle("");
		invalidReviewDTO.setDescription("Review Description");
		invalidReviewDTO.setScore(5L);
		invalidReviewDTO.setIdEvent(pastEvent.getId());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			consumerReviewService.create(invalidReviewDTO)
		);
		assertEquals("Title must not be empty.", exception.getMessage());
	}
}