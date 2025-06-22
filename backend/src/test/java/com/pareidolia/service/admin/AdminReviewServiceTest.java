package com.pareidolia.service.admin;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.Review;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.ReviewRepository;
import com.pareidolia.state.DraftState;
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
public class AdminReviewServiceTest {

	private final String adminEmail = "admin@test.com";
	private final String adminPassword = "TestPassword123#";

	@Autowired
	private AdminReviewService adminReviewService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private ReviewRepository reviewRepository;

	private Account adminAccount;
	private Event testEvent;
	private Review testReview;

	@BeforeEach
	void setUp() {
		// Create admin account
		adminAccount = accountRepository.save(Account.builder()
			.email(adminEmail)
			.password(DigestUtils.sha3_256Hex(adminPassword))
			.name("Test")
			.surname("Admin")
			.phone("+39123456789")
			.referenceType(Account.Type.ADMIN)
			.build());

		// Create test event
		Event event = Event.builder()
			.title("Test Event")
			.description("Test Description")
			.place("Test Place")
			.date(LocalDate.now().minusDays(7))
			.time(LocalTime.of(20, 0))
			.duration(Duration.ofHours(2))
			.maxNumberOfParticipants(100L)
			.state(State.fromString(DraftState.name, null))
			.build();
		testEvent = eventRepository.save(event);
		testEvent.setState(State.fromString(DraftState.name, testEvent));
		testEvent = eventRepository.save(testEvent);

		// Create test message
		testReview = new Review();
		testReview.setTitle("Test review title");
		testReview.setDescription("Test review description");
		testReview.setScore(5L);
		testReview.setIdConsumer(adminAccount.getId());
		testReview.setIdEvent(testEvent.getId());
		testReview.setAccount(adminAccount);
		testReview = reviewRepository.save(testReview);
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetEventReviews() {
		// Act
		Page<ReviewDTO> messages = adminReviewService.getEventReviews(testEvent.getId(), 0, 10);

		// Assert
		assertNotNull(messages);
		assertFalse(messages.isEmpty());
		assertEquals(1, messages.getTotalElements());
		ReviewDTO messageDTO = messages.getContent().get(0);
		assertEquals(testReview.getId(), messageDTO.getId());
		assertEquals(testReview.getTitle(), messageDTO.getTitle());
		assertEquals(testReview.getDescription(), messageDTO.getDescription());
		assertEquals(testReview.getScore(), messageDTO.getScore());
		assertEquals(adminAccount.getId(), messageDTO.getIdConsumer());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreate() {
		// Arrange
		ReviewDTO reviewDTO = new ReviewDTO();
		reviewDTO.setTitle("Test review title");
		reviewDTO.setDescription("Test review description");
		reviewDTO.setScore(5L);
		reviewDTO.setIdEvent(testEvent.getId());
		reviewRepository.delete(testReview);

		// Act
		ReviewDTO createdReviewDTO = adminReviewService.create(reviewDTO);

		// Assert
		assertNotNull(createdReviewDTO);
		assertNotNull(createdReviewDTO.getId());
		assertEquals(testEvent.getId(), createdReviewDTO.getIdEvent());
		assertEquals(reviewDTO.getTitle(), createdReviewDTO.getTitle());
		assertEquals(reviewDTO.getDescription(), createdReviewDTO.getDescription());
		assertEquals(reviewDTO.getScore(), createdReviewDTO.getScore());
		assertEquals(adminAccount.getId(), createdReviewDTO.getIdConsumer());
		assertNotNull(createdReviewDTO.getCreationTime());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDelete() {
		// Act
		adminReviewService.delete(testReview.getId());

		// Assert
		assertFalse(reviewRepository.findById(testReview.getId()).isPresent());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetEventReviewsWithInvalidEventId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminReviewService.getEventReviews(999L, 0, 10)
		);
		assertEquals("Invalid Event ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreateWithInvalidEventId() {
		// Arrange
		ReviewDTO invalidReviewDTO = new ReviewDTO();
		invalidReviewDTO.setTitle("Test review title");
		invalidReviewDTO.setDescription("Test review description");
		invalidReviewDTO.setScore(5L);
		invalidReviewDTO.setIdEvent(999L);
		reviewRepository.delete(testReview);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminReviewService.create(invalidReviewDTO)
		);
		assertEquals("Event not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreateWithEmptyReview() {
		// Arrange
		ReviewDTO invalidReviewDTO = new ReviewDTO();
		invalidReviewDTO.setTitle("Test review title");
		invalidReviewDTO.setDescription("");
		invalidReviewDTO.setScore(5L);
		invalidReviewDTO.setIdEvent(testEvent.getId());
		reviewRepository.delete(testReview);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminReviewService.create(invalidReviewDTO)
		);
		assertEquals("Description must not be empty.", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreateWithNullReview() {
		// Arrange
		ReviewDTO invalidReviewDTO = new ReviewDTO();
		invalidReviewDTO.setTitle("Test review title");
		invalidReviewDTO.setDescription(null);
		invalidReviewDTO.setScore(5L);
		invalidReviewDTO.setIdEvent(testEvent.getId());
		reviewRepository.delete(testReview);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminReviewService.create(invalidReviewDTO)
		);
		assertEquals("Description must not be empty.", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreateWithTooLongReview() {
		// Arrange
		ReviewDTO invalidReviewDTO = new ReviewDTO();
		invalidReviewDTO.setTitle("Test review title");
		invalidReviewDTO.setDescription("a".repeat(2001)); // Create description longer than 1000 chars
		invalidReviewDTO.setScore(5L);
		invalidReviewDTO.setIdEvent(testEvent.getId());
		reviewRepository.delete(testReview);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminReviewService.create(invalidReviewDTO)
		);
		assertEquals("Description is too long.", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreateWithExistingReview() {
		// Arrange
		ReviewDTO invalidReviewDTO = new ReviewDTO();
		invalidReviewDTO.setTitle("Test review title");
		invalidReviewDTO.setDescription("Test review description");
		invalidReviewDTO.setScore(5L);
		invalidReviewDTO.setIdEvent(testEvent.getId());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminReviewService.create(invalidReviewDTO)
		);
		assertEquals("Review already exists.", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminReviewService.delete(999L)
		);
		assertEquals("Review not found", exception.getMessage());
	}
}