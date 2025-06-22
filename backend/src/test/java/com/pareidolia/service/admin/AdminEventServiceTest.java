package com.pareidolia.service.admin;

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
import com.pareidolia.state.PublishedState;
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
import java.util.ArrayList;
import java.util.List;

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
@ContextConfiguration(initializers = AdminEventServiceTest.Initializer.class)
public class AdminEventServiceTest {

	@TempDir
	static File tempDir;
	private final String adminEmail = "admin@test.com";
	private final String adminPassword = "TestPassword123#";
	private final String promoterEmail = "promoter@test.com";
	private final String promoterPassword = "TestPassword123#";
	@Autowired
	private AdminEventService adminEventService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private PromoterInfoRepository promoterInfoRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private EventPromoterAssociationRepository eventPromoterAssociationRepository;
	private Account adminAccount;
	private Account promoterAccount;
	private PromoterInfo promoterInfo;
	private Event testEvent;

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

		// Create promoter account
		promoterAccount = accountRepository.save(Account.builder()
			.email(promoterEmail)
			.password(DigestUtils.sha3_256Hex(promoterPassword))
			.name("Test")
			.surname("Promoter")
			.phone("+39987654321")
			.referenceType(Account.Type.PROMOTER)
			.build());

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
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetEvent() {
		// Act
		EventDTO eventDTO = adminEventService.getEvent(testEvent.getId());

		// Assert
		assertNotNull(eventDTO);
		assertEquals(testEvent.getId(), eventDTO.getId());
		assertEquals(testEvent.getTitle(), eventDTO.getTitle());
		assertEquals(1, eventDTO.getPromoters().size());
		assertEquals(promoterAccount.getId(), eventDTO.getPromoters().get(0).getId());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetEvents() {
		// Act
		Page<EventDTO> events = adminEventService.getEvents(0, 10, DraftState.name);

		// Assert
		assertNotNull(events);
		assertFalse(events.isEmpty());
		assertEquals(1, events.getTotalElements());
		assertEquals(testEvent.getId(), events.getContent().get(0).getId());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetPromoterEvents() {
		// Act
		Page<EventDTO> events = adminEventService.getPromoterEvents(promoterAccount.getId(), 0, 10, DraftState.name);

		// Assert
		assertNotNull(events);
		assertFalse(events.isEmpty());
		assertEquals(1, events.getTotalElements());
		assertEquals(testEvent.getId(), events.getContent().get(0).getId());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
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
		eventUpdateDTO.setState(DraftState.name);

		List<String> promoters = new ArrayList<>();
		promoters.add(promoterAccount.getEmail());
		eventUpdateDTO.setPromoterEmails(promoters);

		// Act
		EventDTO createdEventDTO = adminEventService.create(eventUpdateDTO);

		// Assert
		assertNotNull(createdEventDTO);
		assertNotNull(createdEventDTO.getId());
		assertEquals(eventUpdateDTO.getTitle(), createdEventDTO.getTitle());
		assertEquals(1, createdEventDTO.getPromoters().size());
		assertEquals(promoterAccount.getId(), createdEventDTO.getPromoters().get(0).getId());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
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

		List<String> promoters = new ArrayList<>();
		promoters.add(promoterAccount.getEmail());
		eventUpdateDTO.setPromoterEmails(promoters);

		// Act
		EventDTO updatedDTO = adminEventService.update(eventUpdateDTO);

		// Assert
		assertNotNull(updatedDTO);
		assertEquals(eventUpdateDTO.getId(), updatedDTO.getId());
		assertEquals(eventUpdateDTO.getTitle(), updatedDTO.getTitle());
		assertEquals(eventUpdateDTO.getDescription(), updatedDTO.getDescription());
		assertEquals(1, updatedDTO.getPromoters().size());
		assertEquals(promoterAccount.getId(), updatedDTO.getPromoters().get(0).getId());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDelete() {
		// Act
		adminEventService.delete(testEvent.getId());

		// Assert
		assertFalse(eventRepository.findById(testEvent.getId()).isPresent());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testMoveBackwards() {
		// Arrange
		testEvent.setState(new PublishedState(testEvent));
		testEvent = eventRepository.save(testEvent);

		// Act
		EventDTO movedEventDTO = adminEventService.moveBackwards(testEvent.getId());

		// Assert
		assertNotNull(movedEventDTO);
		assertEquals(DraftState.name, movedEventDTO.getState());

		Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
		assertEquals(DraftState.name, updatedEvent.getState().getStateName());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testMoveForward() {
		// Act
		EventDTO movedEventDTO = adminEventService.moveForward(testEvent.getId());

		// Assert
		assertNotNull(movedEventDTO);
		assertEquals(ReviewState.name, movedEventDTO.getState());

		Event updatedEvent = eventRepository.findById(testEvent.getId()).orElseThrow();
		assertEquals(ReviewState.name, updatedEvent.getState().getStateName());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetEventWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminEventService.getEvent(999L)
		);
		assertEquals("Invalid Event ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdateWithInvalidId() {
		// Arrange
		EventUpdateDTO invalidDTO = new EventUpdateDTO();
		invalidDTO.setId(999L);
		invalidDTO.setTitle("Invalid Event");

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminEventService.update(invalidDTO)
		);
		assertEquals("Event not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminEventService.delete(999L)
		);
		assertEquals("Event not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testMoveBackwardsWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminEventService.moveBackwards(999L)
		);
		assertEquals("Invalid Event ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testMoveForwardWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminEventService.moveForward(999L)
		);
		assertEquals("Invalid Event ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdateEventImage() {
		// Arrange
		MockMultipartFile imageFile = new MockMultipartFile(
			"image",
			"test-image.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			TestImageGenerator.generateTestImage()
		);

		// Act
		EventDTO updatedEvent = adminEventService.updateEventImage(testEvent.getId(), imageFile);

		// Assert
		assertNotNull(updatedEvent);
		assertNotNull(updatedEvent.getImage());
		assertThat(updatedEvent.getImage(), CoreMatchers.startsWith("TEST_URL/thumbnail-"));
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteEventImage() {
		// Arrange
		testEvent.setImage("test-image.jpg");
		testEvent = eventRepository.save(testEvent);

		// Act
		EventDTO updatedEvent = adminEventService.deleteEventImage(testEvent.getId());

		// Assert
		assertNotNull(updatedEvent);
		assertNull(updatedEvent.getImage());

		Event eventInDb = eventRepository.findById(testEvent.getId()).orElseThrow();
		assertNull(eventInDb.getImage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
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
			adminEventService.updateEventImage(999L, imageFile)
		);
		assertEquals("Event not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteEventImageForNonExistentEvent() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminEventService.deleteEventImage(999L)
		);
		assertEquals("Event not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testUpdateEventImageWithInvalidFile() {
		// Arrange
		MockMultipartFile imageFile = new MockMultipartFile(
			"image",
			"test.jpg",
			MediaType.IMAGE_JPEG_VALUE,
			"test image content".getBytes()
		);

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminEventService.updateEventImage(testEvent.getId(), imageFile)
		);
		assertEquals("Invalid image file", exception.getMessage());
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
		@Override
		public void initialize(@NotNull ConfigurableApplicationContext context) {
			TestPropertyValues.of("app.upload.dir=" + tempDir).applyTo(context);
		}
	}
}