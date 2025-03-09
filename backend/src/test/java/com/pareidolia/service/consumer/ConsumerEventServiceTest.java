package com.pareidolia.service.consumer;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.dto.EventDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.EventPromoterAssociation;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.PromoterInfoRepository;
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
public class ConsumerEventServiceTest {

	private final String consumerEmail = "consumer@test.com";
	private final String consumerPassword = "TestPassword123#";
	private final String promoterEmail = "promoter@test.com";
	private final String promoterPassword = "TestPassword123#";
	@Autowired
	private ConsumerEventService consumerEventService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private PromoterInfoRepository promoterInfoRepository;
	@Autowired
	private EventPromoterAssociationRepository eventPromoterAssociationRepository;
	private Account consumerAccount;
	private Account promoterAccount;
	private Event testEvent;

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

		// Create promoter account
		promoterAccount = accountRepository.save(Account.builder()
			.email(promoterEmail)
			.password(DigestUtils.sha3_256Hex(promoterPassword))
			.name("Test")
			.surname("Promoter")
			.phone("+39987654321")
			.referenceType(Account.Type.PROMOTER)
			.build());

		promoterInfoRepository.save(PromoterInfo.builder()
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
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testGetEvent() {
		// Act
		EventDTO eventDTO = consumerEventService.getEvent(testEvent.getId());

		// Assert
		assertNotNull(eventDTO);
		assertEquals(testEvent.getId(), eventDTO.getId());
		assertEquals(testEvent.getTitle(), eventDTO.getTitle());
		assertEquals(1, eventDTO.getPromoters().size());
		assertEquals(promoterAccount.getId(), eventDTO.getPromoters().get(0).getId());
	}

	@Test
	@WithMockUser(username = consumerEmail, authorities = {"CONSUMER"})
	void testGetEvents() {
		// Act
		Page<EventDTO> events = consumerEventService.getEvents(0, 10);

		// Assert
		assertNotNull(events);
		assertFalse(events.isEmpty());
		assertEquals(1, events.getTotalElements());
		assertEquals(testEvent.getId(), events.getContent().get(0).getId());
	}

}