package com.pareidolia.service.promoter;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.dto.MessageDTO;
import com.pareidolia.entity.*;
import com.pareidolia.repository.*;
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
public class PromoterMessageServiceTest {

	private final String promoterEmail = "promoter@test.com";
	private final String promoterPassword = "TestPassword123#";

	@Autowired
	private PromoterMessageService promoterMessageService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private PromoterInfoRepository promoterInfoRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private MessageRepository messageRepository;
	@Autowired
	private EventPromoterAssociationRepository eventPromoterAssociationRepository;

	private Account promoterAccount;
	private Event testEvent;
	private Message testMessage;

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

		// Create test message
		testMessage = new Message();
		testMessage.setMessage("Test message content");
		testMessage.setIdAccount(promoterAccount.getId());
		testMessage.setIdEvent(testEvent.getId());
		testMessage.setAccount(promoterAccount);
		testMessage = messageRepository.save(testMessage);
	}

	@Test
	@WithMockUser(username = promoterEmail, authorities = {"PROMOTER"})
	void testGetEventDraftMessages() {
		// Act
		Page<MessageDTO> messages = promoterMessageService.getEventDraftMessages(testEvent.getId(), 0, 10);

		// Assert
		assertNotNull(messages);
		assertFalse(messages.isEmpty());
		assertEquals(1, messages.getTotalElements());
		MessageDTO messageDTO = messages.getContent().get(0);
		assertEquals(testMessage.getId(), messageDTO.getId());
		assertEquals(testMessage.getMessage(), messageDTO.getMessage());
		assertEquals(promoterAccount.getId(), messageDTO.getIdAccount());
	}

	@Test
	@WithMockUser(username = promoterEmail, authorities = {"PROMOTER"})
	void testCreate() {
		// Arrange
		String message = "New test message";

		// Act
		MessageDTO createdMessageDTO = promoterMessageService.create(testEvent.getId(), message);

		// Assert
		assertNotNull(createdMessageDTO);
		assertNotNull(createdMessageDTO.getId());
		assertEquals(message, createdMessageDTO.getMessage());
		assertEquals(promoterAccount.getId(), createdMessageDTO.getIdAccount());
		assertNotNull(createdMessageDTO.getCreationTime());
	}

	@Test
	@WithMockUser(username = promoterEmail, authorities = {"PROMOTER"})
	void testCreateWithInvalidMessage() {
		// Arrange
		String invalidMessage = "";

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			promoterMessageService.create(testEvent.getId(), invalidMessage)
		);
		assertEquals("Message must not be empty.", exception.getMessage());
	}
}