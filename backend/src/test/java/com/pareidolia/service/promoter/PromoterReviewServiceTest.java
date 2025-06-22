package com.pareidolia.service.promoter;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.*;
import com.pareidolia.repository.*;
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
public class PromoterReviewServiceTest {

	private final String promoterEmail = "promoter@test.com";
	private final String promoterPassword = "TestPassword123#";
	@Autowired
	private PromoterReviewService promoterReviewService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private PromoterInfoRepository promoterInfoRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private ReviewRepository reviewRepository;
	@Autowired
	private EventPromoterAssociationRepository eventPromoterAssociationRepository;
	private Account promoterAccount;
	private Event testEvent;
	private Review testReview;

	@BeforeEach
	void setUp() {
		// Create promoter account
		promoterAccount = accountRepository.save(Account.builder()
			.email(promoterEmail)
			.password(DigestUtils.sha3_256Hex(promoterPassword))
			.name("Test")
			.surname("Promoter")
			.phone("+39123456789")
			.referenceType(Account.Type.PROMOTER)
			.build());

		promoterInfoRepository.save(PromoterInfo.builder()
			.idPromoter(promoterAccount.getId())
			.presentation("Test presentation")
			.build());

		// Create test event with past date (for review validation)
		Event event = Event.builder()
			.title("Test Event")
			.description("Test Description")
			.place("Test Place")
			.date(LocalDate.now().minusDays(7))  // Past event
			.time(LocalTime.of(20, 0))
			.duration(Duration.ofHours(2))
			.maxNumberOfParticipants(100L)
			.state(State.fromString(PublishedState.name, null))
			.build();
		testEvent = eventRepository.save(event);
		testEvent.setState(State.fromString(PublishedState.name, testEvent));
		testEvent = eventRepository.save(testEvent);

		// Create association
		EventPromoterAssociation association = new EventPromoterAssociation();
		association.setIdEvent(testEvent.getId());
		association.setIdPromoter(promoterAccount.getId());
		eventPromoterAssociationRepository.save(association);

		// Create test review
		testReview = reviewRepository.save(Review.builder()
			.title("Test Event")
			.description("Test Description")
			.score(5L)
			.idConsumer(promoterAccount.getId())
			.idEvent(testEvent.getId())
			.account(promoterAccount)
			.build());
	}

	@Test
	@WithMockUser(username = promoterEmail, authorities = {"PROMOTER"})
	void testGetEventReviews() {
		// Act
		Page<ReviewDTO> reviews = promoterReviewService.getEventReviews(testEvent.getId(), 0, 10);

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
	@WithMockUser(username = promoterEmail, authorities = {"PROMOTER"})
	void testCreate() {
		// Arrange
		ReviewDTO newReviewDTO = new ReviewDTO();
		newReviewDTO.setTitle("New Review");
		newReviewDTO.setDescription("New Review Description");
		newReviewDTO.setScore(4L);
		newReviewDTO.setIdConsumer(promoterAccount.getId());
		newReviewDTO.setIdEvent(testEvent.getId());
		reviewRepository.delete(testReview);

		// Act
		ReviewDTO createdReviewDTO = promoterReviewService.create(newReviewDTO);

		// Assert
		assertNotNull(createdReviewDTO);
		assertNotNull(createdReviewDTO.getId());
		assertEquals(newReviewDTO.getTitle(), createdReviewDTO.getTitle());
		assertEquals(newReviewDTO.getDescription(), createdReviewDTO.getDescription());
		assertEquals(newReviewDTO.getScore(), createdReviewDTO.getScore());
		assertEquals(promoterAccount.getId(), createdReviewDTO.getIdConsumer());
	}

	@Test
	@WithMockUser(username = promoterEmail, authorities = {"PROMOTER"})
	void testCreateWithInvalidScore() {
		// Arrange
		ReviewDTO newReviewDTO = new ReviewDTO();
		newReviewDTO.setTitle("Invalid Review");
		newReviewDTO.setDescription("Invalid Review Description");
		newReviewDTO.setScore(6L);
		newReviewDTO.setIdConsumer(promoterAccount.getId());
		newReviewDTO.setIdEvent(testEvent.getId());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterReviewService.create(newReviewDTO)
		);
		assertEquals("Score must be between 1 and 5.", exception.getMessage());
	}

	@Test
	@WithMockUser(username = promoterEmail, authorities = {"PROMOTER"})
	void testCreateReviewForFutureEvent() {
		// Arrange
		Event futureEvent = Event.builder()
			.title("Future Event")
			.description("Future Event Description")
			.place("Test Place")
			.date(LocalDate.now().plusDays(7))  // Future event
			.time(LocalTime.of(20, 0))
			.duration(Duration.ofHours(2))
			.maxNumberOfParticipants(100L)
			.state(State.fromString(PublishedState.name, null))
			.build();
		futureEvent = eventRepository.save(futureEvent);
		futureEvent.setState(State.fromString(PublishedState.name, futureEvent));
		futureEvent = eventRepository.save(futureEvent);

		ReviewDTO newReviewDTO = new ReviewDTO();
		newReviewDTO.setTitle("Invalid Review");
		newReviewDTO.setDescription("Invalid Review Description");
		newReviewDTO.setScore(5L);
		newReviewDTO.setIdConsumer(promoterAccount.getId());
		newReviewDTO.setIdEvent(futureEvent.getId());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterReviewService.create(newReviewDTO)
		);
		assertEquals("Event has not finished yet.", exception.getMessage());
	}
}