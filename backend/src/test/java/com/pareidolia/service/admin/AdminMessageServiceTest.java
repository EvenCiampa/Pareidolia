package com.pareidolia.service.admin;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.dto.MessageDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.Message;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.MessageRepository;
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
public class AdminMessageServiceTest {

	private final String adminEmail = "admin@test.com";
	private final String adminPassword = "TestPassword123#";

	@Autowired
	private AdminMessageService adminMessageService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private MessageRepository messageRepository;

	private Account adminAccount;
	private Event testEvent;
	private Message testMessage;

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
			.date(LocalDate.now().plusDays(7))
			.time(LocalTime.of(20, 0))
			.duration(Duration.ofHours(2))
			.maxNumberOfParticipants(100L)
			.state(State.fromString(DraftState.name, null))
			.build();
		testEvent = eventRepository.save(event);
		testEvent.setState(State.fromString(DraftState.name, testEvent));
		testEvent = eventRepository.save(testEvent);

		// Create test message
		testMessage = new Message();
		testMessage.setMessage("Test message content");
		testMessage.setIdAccount(adminAccount.getId());
		testMessage.setIdEvent(testEvent.getId());
		testMessage.setAccount(adminAccount);
		testMessage = messageRepository.save(testMessage);
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetEventDraftMessages() {
		// Act
		Page<MessageDTO> messages = adminMessageService.getEventDraftMessages(testEvent.getId(), 0, 10);

		// Assert
		assertNotNull(messages);
		assertFalse(messages.isEmpty());
		assertEquals(1, messages.getTotalElements());
		MessageDTO messageDTO = messages.getContent().get(0);
		assertEquals(testMessage.getId(), messageDTO.getId());
		assertEquals(testMessage.getMessage(), messageDTO.getMessage());
		assertEquals(adminAccount.getId(), messageDTO.getIdAccount());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreate() {
		// Arrange
		String newMessage = "New test message from admin";

		// Act
		MessageDTO createdMessageDTO = adminMessageService.create(testEvent.getId(), newMessage);

		// Assert
		assertNotNull(createdMessageDTO);
		assertNotNull(createdMessageDTO.getId());
		assertEquals(newMessage, createdMessageDTO.getMessage());
		assertEquals(adminAccount.getId(), createdMessageDTO.getIdAccount());
		assertNotNull(createdMessageDTO.getCreationTime());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDelete() {
		// Act
		adminMessageService.delete(testMessage.getId());

		// Assert
		assertFalse(messageRepository.findById(testMessage.getId()).isPresent());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetEventDraftMessagesWithInvalidEventId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminMessageService.getEventDraftMessages(999L, 0, 10)
		);
		assertEquals("EventDraft not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreateWithInvalidEventId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminMessageService.create(999L, "Test message")
		);
		assertEquals("EventDraft not found", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreateWithEmptyMessage() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminMessageService.create(testEvent.getId(), "")
		);
		assertEquals("Message must not be empty.", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreateWithNullMessage() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminMessageService.create(testEvent.getId(), null)
		);
		assertEquals("Message must not be empty.", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testCreateWithTooLongMessage() {
		// Arrange
		String longMessage = "a".repeat(1001); // Create message longer than 1000 chars

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminMessageService.create(testEvent.getId(), longMessage)
		);
		assertEquals("Message is too long.", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminMessageService.delete(999L)
		);
		assertEquals("Message not found", exception.getMessage());
	}
}