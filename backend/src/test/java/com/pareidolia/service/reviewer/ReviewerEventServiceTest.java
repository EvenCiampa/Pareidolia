package com.pareidolia.service.reviewer;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.ReviewerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.BookingRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.repository.model.EventWithInfoForAccount;
import com.pareidolia.service.ImageService;
import com.pareidolia.state.DraftState;
import com.pareidolia.state.ReviewState;
import com.pareidolia.state.State;
import com.pareidolia.validator.EventDraftValidator;
import com.pareidolia.validator.EventValidator;
import com.pareidolia.validator.ImageValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.util.Pair;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewerEventServiceTest {

	@Mock
	private ImageService imageService;

	@Mock
	private ImageValidator imageValidator;

	@Mock
	private EventValidator eventValidator;

	@Mock
	private EventRepository eventRepository;

	@Mock
	private ReviewerService reviewerService;

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private EventDraftValidator eventDraftValidator;

	@Mock
	private PromoterInfoRepository promoterInfoRepository;

	@Mock
	private EventPromoterAssociationRepository eventPromoterAssociationRepository;

	@InjectMocks
	private ReviewerEventService reviewerEventService;

	private Event testEvent;
	private Account testAccount;
	private ReviewerDTO testReviewerDTO;
	private PromoterInfo testPromoterInfo;
	private List<Pair<Account, PromoterInfo>> testPromoterPairs;
	private EventWithInfoForAccount testEventWithInfo;

	@BeforeEach
	void setUp() {
		testAccount = new Account();
		testAccount.setId(1L);
		testAccount.setEmail("test@example.com");
		testAccount.setReferenceType(Account.Type.REVIEWER);

		testReviewerDTO = new ReviewerDTO();
		testReviewerDTO.id = testAccount.getId();
		testReviewerDTO.email = testAccount.getEmail();

		testEvent = new Event();
		testEvent.setId(1L);
		testEvent.setTitle("Test Event");
		testEvent.setDate(LocalDate.now());
		testEvent.setTime(LocalTime.now());
		testEvent.setDuration(Duration.ofHours(2));
		testEvent.setMaxNumberOfParticipants(100L);
		testEvent.setPlace("Test Place");
		testEvent.setState(State.fromString(DraftState.name, testEvent));

		Account promoterAccount = new Account();
		promoterAccount.setId(2L);
		promoterAccount.setReferenceType(Account.Type.PROMOTER);

		testPromoterInfo = new PromoterInfo();
		testPromoterInfo.setIdPromoter(promoterAccount.getId());

		testPromoterPairs = Collections.singletonList(Pair.of(promoterAccount, testPromoterInfo));

		testEventWithInfo = new EventWithInfoForAccount(testEvent, false, 50L);
	}

	@Test
	void testGetEventSuccess() {
		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(bookingRepository.countByIdEvent(testEvent.getId())).thenReturn(50L);
		when(bookingRepository.findByIdEventAndIdAccount(testEvent.getId(), testReviewerDTO.id))
			.thenReturn(Optional.empty());
		when(eventPromoterAssociationRepository.findPromotersByIdEvent(testEvent.getId()))
			.thenReturn(testPromoterPairs);
		when(reviewerService.getData()).thenReturn(testReviewerDTO);

		EventDTO result = reviewerEventService.getEvent(testEvent.getId());

		assertNotNull(result);
		assertEquals(testEvent.getId(), result.id);
		assertEquals(testEvent.getTitle(), result.title);
		assertFalse(result.booked);
	}

	@Test
	void testGetEventInvalidId() {
		when(eventRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerEventService.getEvent(999L));
	}

	@Test
	void testGetEventsWithoutState() {
		List<EventWithInfoForAccount> events = Collections.singletonList(testEventWithInfo);
		Page<EventWithInfoForAccount> eventPage = new PageImpl<>(events);

		when(reviewerService.getData()).thenReturn(testReviewerDTO);
		when(eventRepository.findAllByAccountIdWithCount(eq(testReviewerDTO.id), any(PageRequest.class)))
			.thenReturn(eventPage);
		when(eventPromoterAssociationRepository.findPromotersByIdEvent(testEvent.getId()))
			.thenReturn(testPromoterPairs);

		Page<EventDTO> result = reviewerEventService.getEvents(0, 10, null);

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
		assertEquals(1, result.getContent().size());
		assertEquals(testEvent.getId(), result.getContent().get(0).id);
	}

	@Test
	void testGetEventsWithState() {
		List<EventWithInfoForAccount> events = Collections.singletonList(testEventWithInfo);
		Page<EventWithInfoForAccount> eventPage = new PageImpl<>(events);

		when(reviewerService.getData()).thenReturn(testReviewerDTO);
		when(eventRepository.findAllByAccountIdAndState(eq(testReviewerDTO.id), eq("DRAFT"), any(PageRequest.class)))
			.thenReturn(eventPage);
		when(eventPromoterAssociationRepository.findPromotersByIdEvent(testEvent.getId()))
			.thenReturn(testPromoterPairs);

		Page<EventDTO> result = reviewerEventService.getEvents(0, 10, "DRAFT");

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
		assertEquals(1, result.getContent().size());
		assertEquals(testEvent.getId(), result.getContent().get(0).id);
	}

	@Test
	void testGetPromoterEventsWithoutState() {
		List<EventWithInfoForAccount> events = Collections.singletonList(testEventWithInfo);
		Page<EventWithInfoForAccount> eventPage = new PageImpl<>(events);

		when(reviewerService.getData()).thenReturn(testReviewerDTO);
		when(eventRepository.findAllByAccountIdAndPromoterId(eq(testReviewerDTO.id), eq(2L), any(PageRequest.class)))
			.thenReturn(eventPage);
		when(eventPromoterAssociationRepository.findPromotersByIdEvent(testEvent.getId()))
			.thenReturn(testPromoterPairs);

		Page<EventDTO> result = reviewerEventService.getPromoterEvents(2L, 0, 10, null);

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
		assertEquals(1, result.getContent().size());
		assertEquals(testEvent.getId(), result.getContent().get(0).id);
	}

	@Test
	void testGetPromoterEventsWithState() {
		List<EventWithInfoForAccount> events = Collections.singletonList(testEventWithInfo);
		Page<EventWithInfoForAccount> eventPage = new PageImpl<>(events);

		when(reviewerService.getData()).thenReturn(testReviewerDTO);
		when(eventRepository.findAllByAccountIdAndStateAndPromoterId(
			eq(testReviewerDTO.id), eq("DRAFT"), eq(2L), any(PageRequest.class)))
			.thenReturn(eventPage);
		when(eventPromoterAssociationRepository.findPromotersByIdEvent(testEvent.getId()))
			.thenReturn(testPromoterPairs);

		Page<EventDTO> result = reviewerEventService.getPromoterEvents(2L, 0, 10, "DRAFT");

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
		assertEquals(1, result.getContent().size());
		assertEquals(testEvent.getId(), result.getContent().get(0).id);
	}

	@Test
	void testMoveBackwards() {
		testEvent.setState(State.fromString(ReviewState.name, testEvent));
		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
		when(eventPromoterAssociationRepository.findPromotersByIdEvent(testEvent.getId()))
			.thenReturn(testPromoterPairs);
		when(bookingRepository.findByIdEventAndIdAccount(testEvent.getId(), testReviewerDTO.id))
			.thenReturn(Optional.empty());
		when(bookingRepository.countByIdEvent(testEvent.getId())).thenReturn(50L);
		when(reviewerService.getData()).thenReturn(testReviewerDTO);

		EventDTO result = reviewerEventService.moveBackwards(testEvent.getId());

		assertNotNull(result);
		assertEquals(testEvent.getId(), result.id);
		verify(eventRepository).save(any(Event.class));
	}

	@Test
	void testMoveForward() {
		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
		when(eventPromoterAssociationRepository.findPromotersByIdEvent(testEvent.getId()))
			.thenReturn(testPromoterPairs);
		when(bookingRepository.findByIdEventAndIdAccount(testEvent.getId(), testReviewerDTO.id))
			.thenReturn(Optional.empty());
		when(bookingRepository.countByIdEvent(testEvent.getId())).thenReturn(50L);
		when(reviewerService.getData()).thenReturn(testReviewerDTO);

		EventDTO result = reviewerEventService.moveForward(testEvent.getId());

		assertNotNull(result);
		assertEquals(testEvent.getId(), result.id);
		verify(eventRepository).save(any(Event.class));
	}

	@Test
	void testUpdateEventImage() throws IOException {
		MockMultipartFile imageFile = new MockMultipartFile(
			"image", "test.jpg", "image/jpeg", "test image content".getBytes());

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(imageService.saveImage(imageFile)).thenReturn("test.jpg");
		when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
		when(eventPromoterAssociationRepository.findPromotersByIdEvent(testEvent.getId()))
			.thenReturn(testPromoterPairs);
		when(bookingRepository.findByIdEventAndIdAccount(testEvent.getId(), testReviewerDTO.id))
			.thenReturn(Optional.empty());
		when(bookingRepository.countByIdEvent(testEvent.getId())).thenReturn(50L);
		when(reviewerService.getData()).thenReturn(testReviewerDTO);

		EventDTO result = reviewerEventService.updateEventImage(testEvent.getId(), imageFile);

		assertNotNull(result);
		assertEquals(testEvent.getId(), result.id);
		verify(imageValidator).validateEventImage(imageFile);
		verify(imageService).saveImage(imageFile);
		verify(eventRepository).save(any(Event.class));
	}

	@Test
	void testUpdateEventImageInvalidEvent() {
		MockMultipartFile imageFile = new MockMultipartFile(
			"image", "test.jpg", "image/jpeg", "test image content".getBytes());

		when(eventRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerEventService.updateEventImage(999L, imageFile));
	}

	@Test
	void testDeleteEventImage() {
		testEvent.setImage("existing.jpg");
		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
		when(eventPromoterAssociationRepository.findPromotersByIdEvent(testEvent.getId()))
			.thenReturn(testPromoterPairs);
		when(bookingRepository.findByIdEventAndIdAccount(testEvent.getId(), testReviewerDTO.id))
			.thenReturn(Optional.empty());
		when(bookingRepository.countByIdEvent(testEvent.getId())).thenReturn(50L);
		when(reviewerService.getData()).thenReturn(testReviewerDTO);

		EventDTO result = reviewerEventService.deleteEventImage(testEvent.getId());

		assertNotNull(result);
		assertEquals(testEvent.getId(), result.id);
		assertNull(testEvent.getImage());
		verify(eventRepository).save(any(Event.class));
	}

	@Test
	void testDeleteEventImageInvalidEvent() {
		when(eventRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerEventService.deleteEventImage(999L));
	}
} 