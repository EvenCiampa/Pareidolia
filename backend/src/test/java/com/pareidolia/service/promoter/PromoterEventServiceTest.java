package com.pareidolia.service.promoter;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.EventUpdateDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.EventPromoterAssociation;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.PromoterInfoRepository;
import com.pareidolia.state.DraftState;
import com.pareidolia.state.ReviewState;
import com.pareidolia.state.State;
import com.pareidolia.util.TestImageGenerator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.File;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Transactional
@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = PromoterEventServiceTest.Initializer.class)
public class PromoterEventServiceTest {

	@TempDir
	static File tempDir;
	private final String accountEmail = "promoter@test.com";
	private final String accountPassword = "TestPassword123#";
	@Autowired
	private PromoterEventService promoterEventService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private PromoterInfoRepository promoterInfoRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private EventPromoterAssociationRepository eventPromoterAssociationRepository;
	private Account promoterAccount;
	private PromoterInfo promoterInfo;
	private Event testEvent;

	@BeforeEach
	void setUp() {
		// Create promoter account
		promoterAccount = accountRepository.save(Account.builder()
			.email(accountEmail)
			.password(DigestUtils.sha3_256Hex(accountPassword))
			.name("Test")
			.surname("Promoter")
			.phone("+39123456789")
			.referenceType(Account.Type.PROMOTER)
			.build());

		// Create promoter info
		promoterInfo = promoterInfoRepository.save(PromoterInfo.builder()
			.idPromoter(promoterAccount.getId())
			.presentation("Test presentation")
			.account(promoterAccount)
			.build());

		// Create test event
		Event event = Event.builder()
			.title("Test Event")
			.description("Test Description")
			.place("Test Place")
			.date(LocalDate.now().plusDays(7))
			.time(LocalTime.of(20, 0))
			.duration(Duration.ofHours(2))
			.maxNumberOfParticipants(100L)
			.state(State.fromString(DraftState.name, null))
			.build();
		testEvent = eventRepository.save(event);
		testEvent.setState(State.fromString(DraftState.name, testEvent));
		testEvent = eventRepository.save(testEvent);

		// Create association
		EventPromoterAssociation association = new EventPromoterAssociation();
		association.setIdEvent(testEvent.getId());
		association.setIdPromoter(promoterAccount.getId());
		eventPromoterAssociationRepository.save(association);
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testGetEventDraft() {
		// Act
		EventDTO eventDTO = promoterEventService.getEventDraft(testEvent.getId());

		// Assert
		assertNotNull(eventDTO);
		assertEquals(testEvent.getTitle(), eventDTO.getTitle());
		assertEquals(testEvent.getDescription(), eventDTO.getDescription());
		assertEquals(1, eventDTO.getPromoters().size());
		assertEquals(promoterAccount.getId(), eventDTO.getPromoters().get(0).getId());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testGetEvents() {
		// Act
		Page<EventDTO> events = promoterEventService.getEvents(0, 10, DraftState.name);

		// Assert
		assertNotNull(events);
		assertFalse(events.isEmpty());
		assertEquals(1, events.getTotalElements());
		assertEquals(testEvent.getId(), events.getContent().get(0).getId());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testCreate() {
		// Arrange
		EventUpdateDTO eventUpdateDTO = new EventUpdateDTO();
		eventUpdateDTO.setTitle("New Test Event");
		eventUpdateDTO.setDescription("New Description");
		eventUpdateDTO.setPlace("New Place");
		eventUpdateDTO.setDate(LocalDate.now().plusDays(14));
		eventUpdateDTO.setTime(LocalTime.of(21, 0));
		eventUpdateDTO.setDuration(Duration.ofHours(3));
		eventUpdateDTO.setMaxNumberOfParticipants(200L);

		// Act
		EventDTO createdEventDTO = promoterEventService.create(eventUpdateDTO);

		// Assert
		assertNotNull(createdEventDTO);
		assertNotNull(createdEventDTO.getId());
		assertEquals(eventUpdateDTO.getTitle(), createdEventDTO.getTitle());
		assertEquals(1, createdEventDTO.getPromoters().size());
		assertEquals(promoterAccount.getId(), createdEventDTO.getPromoters().get(0).getId());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testSubmitForReview() {
		// Act
		EventDTO reviewEventDTO = promoterEventService.submitForReview(testEvent.getId());

		// Assert
		assertNotNull(reviewEventDTO);
		assertEquals(ReviewState.name, reviewEventDTO.getState());

		Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
		assertEquals(ReviewState.name, updatedEvent.getState().getStateName());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testAddPromoterToEventDraft() {
		// Arrange
		String promoterEmail = "new.promoter@test.com";
		String promoterPassword = "TestPassword123#";
		Account newPromoterAccount = accountRepository.save(Account.builder()
			.email(promoterEmail)
			.password(DigestUtils.sha3_256Hex(promoterPassword))
			.name("New")
			.surname("Promoter")
			.phone("+39111222333")
			.referenceType(Account.Type.PROMOTER)
			.build());

		promoterInfoRepository.save(PromoterInfo.builder()
			.idPromoter(newPromoterAccount.getId())
			.presentation("New promoter presentation")
			.build());

		// Act
		EventDTO updatedEventDTO = promoterEventService.addPromoterToEventDraft(testEvent.getId(), newPromoterAccount.getId());

		// Assert
		assertNotNull(updatedEventDTO);
		assertEquals(2, updatedEventDTO.getPromoters().size());
		assertTrue(updatedEventDTO.getPromoters().stream()
			.anyMatch(p -> p.getId().equals(promoterAccount.getId())));
		assertTrue(updatedEventDTO.getPromoters().stream()
			.anyMatch(p -> p.getId().equals(newPromoterAccount.getId())));

		// Verify database update
		assertTrue(eventPromoterAssociationRepository
			.findByIdEventAndIdPromoter(testEvent.getId(), newPromoterAccount.getId())
			.isPresent());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testAddPromoterToEventDraftWithInvalidEventId() {
		// Arrange
		String promoterEmail = "new.promoter@test.com";
		String promoterPassword = "TestPassword123#";
		Account newPromoterAccount = accountRepository.save(Account.builder()
			.email(promoterEmail)
			.password(DigestUtils.sha3_256Hex(promoterPassword))
			.name("New")
			.surname("Promoter")
			.phone("+39111222333")
			.referenceType(Account.Type.PROMOTER)
			.build());

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterEventService.addPromoterToEventDraft(999L, newPromoterAccount.getId())
		);
		assertEquals("Invalid EventDraft ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testAddPromoterToEventDraftWithInvalidPromoterId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterEventService.addPromoterToEventDraft(testEvent.getId(), 999L)
		);
		assertEquals("Account not found.", exception.getMessage());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdate() {
		// Arrange
		EventUpdateDTO eventUpdateDTO = new EventUpdateDTO();
		eventUpdateDTO.setId(testEvent.getId());
		eventUpdateDTO.setTitle("Updated Event");
		eventUpdateDTO.setDescription("Updated Description");
		eventUpdateDTO.setPlace("Updated Place");
		eventUpdateDTO.setDate(LocalDate.now().plusDays(21));
		eventUpdateDTO.setTime(LocalTime.of(22, 0));
		eventUpdateDTO.setDuration(Duration.ofHours(4));
		eventUpdateDTO.setMaxNumberOfParticipants(300L);
		eventUpdateDTO.setState(DraftState.name);

		// Act
		EventDTO updatedDTO = promoterEventService.update(eventUpdateDTO);

		// Assert
		assertNotNull(updatedDTO);
		assertEquals(eventUpdateDTO.getId(), updatedDTO.getId());
		assertEquals(eventUpdateDTO.getTitle(), updatedDTO.getTitle());
		assertEquals(eventUpdateDTO.getDescription(), updatedDTO.getDescription());
		assertEquals(eventUpdateDTO.getPlace(), updatedDTO.getPlace());
		assertEquals(eventUpdateDTO.getDate(), updatedDTO.getDate());
		assertEquals(eventUpdateDTO.getTime(), updatedDTO.getTime());
		assertEquals(eventUpdateDTO.getDuration(), updatedDTO.getDuration());
		assertEquals(eventUpdateDTO.getMaxNumberOfParticipants(), updatedDTO.getMaxNumberOfParticipants());
		assertEquals(1, updatedDTO.getPromoters().size());
		assertEquals(promoterAccount.getId(), updatedDTO.getPromoters().get(0).getId());

		// Verify database update
		Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
		assertEquals(eventUpdateDTO.getTitle(), updatedEvent.getTitle());
		assertEquals(eventUpdateDTO.getDescription(), updatedEvent.getDescription());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdateWithInvalidId() {
		// Arrange
		EventUpdateDTO invalidDTO = new EventUpdateDTO();
		invalidDTO.setId(999L);
		invalidDTO.setTitle("Invalid Event");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterEventService.update(invalidDTO)
		);
		assertEquals("Event not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdateWithNullId() {
		// Arrange
		EventUpdateDTO invalidDTO = new EventUpdateDTO();
		invalidDTO.setTitle("Invalid Event");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterEventService.update(invalidDTO)
		);
		assertEquals("Invalid EventDraft ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdateEventNotOwnedByPromoter() {
		// Arrange
		String promoterEmail = "other.promoter@test.com";
		String promoterPassword = "TestPassword123#";
		Account otherPromoter = accountRepository.save(Account.builder()
			.email(promoterEmail)
			.password(DigestUtils.sha3_256Hex(promoterPassword))
			.name("Other")
			.surname("Promoter")
			.phone("+39444555666")
			.referenceType(Account.Type.PROMOTER)
			.build());

		promoterInfoRepository.save(PromoterInfo.builder()
			.idPromoter(otherPromoter.getId())
			.presentation("Test presentation")
			.build());

		Event otherEvent = eventRepository.save(Event.builder()
			.title("Other Event")
			.description("Other Description")
			.place("Other Place")
			.date(LocalDate.now().plusDays(7))
			.time(LocalTime.of(20, 0))
			.duration(Duration.ofHours(2))
			.maxNumberOfParticipants(100L)
			.state(State.fromString(DraftState.name, null))
			.build());

		testEvent.setState(State.fromString(DraftState.name, testEvent));

		EventPromoterAssociation otherAssociation = new EventPromoterAssociation();
		otherAssociation.setIdEvent(otherEvent.getId());
		otherAssociation.setIdPromoter(otherPromoter.getId());
		eventPromoterAssociationRepository.save(otherAssociation);

		EventUpdateDTO updateDTO = new EventUpdateDTO();
		updateDTO.setId(otherEvent.getId());
		updateDTO.setTitle("Updated Event");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterEventService.update(updateDTO)
		);
		assertEquals("Invalid EventDraft ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdateEventImage() {
		// Arrange
		MockMultipartFile imageFile = new MockMultipartFile(
			"image",
			"test-image.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			TestImageGenerator.generateTestImage()
		);

		// Act
		EventDTO updatedEvent = promoterEventService.updateEventImage(testEvent.getId(), imageFile);

		// Assert
		assertNotNull(updatedEvent);
		assertNotNull(updatedEvent.getImage());
		assertThat(updatedEvent.getImage(), CoreMatchers.startsWith("TEST_URL/thumbnail-"));
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testDeleteEventImage() {
		// Arrange
		testEvent.setImage("test-image.jpg");
		testEvent = eventRepository.save(testEvent);

		// Act
		EventDTO updatedEvent = promoterEventService.deleteEventImage(testEvent.getId());

		// Assert
		assertNotNull(updatedEvent);
		assertNull(updatedEvent.getImage());

		Event eventInDb = eventRepository.findById(testEvent.getId()).orElseThrow();
		assertNull(eventInDb.getImage());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdateEventImageForNonExistentEvent() {
		// Arrange
		MockMultipartFile imageFile = new MockMultipartFile(
			"image",
			"test.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			TestImageGenerator.generateTestImage()
		);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterEventService.updateEventImage(999L, imageFile)
		);
		assertEquals("Event not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testDeleteEventImageForNonExistentEvent() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterEventService.deleteEventImage(999L)
		);
		assertEquals("Event not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = accountEmail, authorities = {"PROMOTER"})
	void testUpdateEventImageWithInvalidFile() {
		// Arrange
		MockMultipartFile imageFile = new MockMultipartFile(
			"image",
			"test.gif",
			MediaType.IMAGE_GIF_VALUE,
			"test image content".getBytes()
		);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterEventService.updateEventImage(testEvent.getId(), imageFile)
		);
		assertEquals("Invalid file extension. Allowed: png, jpg, jpeg", exception.getMessage());
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(@NotNull ConfigurableApplicationContext context) {
			TestPropertyValues.of("app.upload.dir=" + tempDir).applyTo(context);
		}
	}
}