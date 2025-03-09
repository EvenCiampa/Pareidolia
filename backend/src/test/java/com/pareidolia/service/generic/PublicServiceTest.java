package com.pareidolia.service.generic;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
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
import org.springframework.data.util.Pair;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@Transactional
@SpringJUnitConfig
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = NONE)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class PublicServiceTest {

	private final String promoterEmail = "promoter@test.com";
	private final String promoterPassword = "TestPassword123#";
	@Autowired
	private PublicService publicService;
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
	private Event publishedEvent;

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

		promoterInfo = promoterInfoRepository.save(PromoterInfo.builder()
			.idPromoter(promoterAccount.getId())
			.presentation("Test presentation")
			.build());

		// Create published event
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
		publishedEvent = eventRepository.save(event);
		publishedEvent.setState(State.fromString(PublishedState.name, publishedEvent));
		publishedEvent = eventRepository.save(publishedEvent);

		// Create association
		EventPromoterAssociation association = new EventPromoterAssociation();
		association.setIdEvent(publishedEvent.getId());
		association.setIdPromoter(promoterAccount.getId());
		eventPromoterAssociationRepository.save(association);
	}

	// Tests without authentication
	@Test
	void testGetEvent() {
		// Act
		EventDTO eventDTO = publicService.getEvent(publishedEvent.getId());

		// Assert
		assertNotNull(eventDTO);
		assertEquals(publishedEvent.getId(), eventDTO.getId());
		assertEquals(publishedEvent.getTitle(), eventDTO.getTitle());
		assertEquals(1, eventDTO.getPromoters().size());
		assertEquals(promoterAccount.getId(), eventDTO.getPromoters().get(0).getId());
	}

	@Test
	void testGetEvents() {
		// Act
		Page<EventDTO> events = publicService.getEvents(0, 10);

		// Assert
		assertNotNull(events);
		assertFalse(events.isEmpty());
		assertEquals(1, events.getTotalElements());
		assertEquals(publishedEvent.getId(), events.getContent().get(0).getId());
	}

	@Test
	void testFindPromotersByEventId() {
		// Act
		List<Pair<Account, PromoterInfo>> promoters = publicService.findPromotersByEventId(publishedEvent.getId());

		// Assert
		assertNotNull(promoters);
		assertEquals(1, promoters.size());
		assertEquals(promoterAccount.getId(), promoters.get(0).getFirst().getId());
		assertEquals(promoterInfo.getId(), promoters.get(0).getSecond().getId());
	}

	@Test
	void testGetPromoter() {
		// Act
		PromoterDTO promoterDTO = publicService.getPromoter(promoterAccount.getId());

		// Assert
		assertNotNull(promoterDTO);
		assertEquals(promoterAccount.getId(), promoterDTO.getId());
		assertEquals(promoterAccount.getEmail(), promoterDTO.getEmail());
		assertEquals(promoterInfo.getPresentation(), promoterDTO.getPresentation());
	}

	@Test
	void testGetPromoters() {
		// Act
		Page<PromoterDTO> promoters = publicService.getPromoters(0, 10);

		// Assert
		assertNotNull(promoters);
		assertFalse(promoters.isEmpty());
		assertEquals(1, promoters.getTotalElements());
		assertEquals(promoterAccount.getId(), promoters.getContent().get(0).getId());
	}

	@Test
	void testGetPromoterEvents() {
		// Act
		Page<EventDTO> events = publicService.getPromoterEvents(promoterAccount.getId(), 0, 10);

		// Assert
		assertNotNull(events);
		assertFalse(events.isEmpty());
		assertEquals(1, events.getTotalElements());
		assertEquals(publishedEvent.getId(), events.getContent().get(0).getId());
	}

	// Tests with CONSUMER authentication
	@Test
	@WithMockUser(username = "consumer@test.com", authorities = {"CONSUMER"})
	void testGetEventAsConsumer() {
		// Act
		EventDTO eventDTO = publicService.getEvent(publishedEvent.getId());

		// Assert
		assertNotNull(eventDTO);
		assertEquals(publishedEvent.getId(), eventDTO.getId());
	}

	// Tests with PROMOTER authentication
	@Test
	@WithMockUser(username = "promoter@test.com", authorities = {"PROMOTER"})
	void testGetEventAsPromoter() {
		// Act
		EventDTO eventDTO = publicService.getEvent(publishedEvent.getId());

		// Assert
		assertNotNull(eventDTO);
		assertEquals(publishedEvent.getId(), eventDTO.getId());
	}

	// Tests with ADMIN authentication
	@Test
	@WithMockUser(username = "admin@test.com", authorities = {"ADMIN"})
	void testGetEventAsAdmin() {
		// Act
		EventDTO eventDTO = publicService.getEvent(publishedEvent.getId());

		// Assert
		assertNotNull(eventDTO);
		assertEquals(publishedEvent.getId(), eventDTO.getId());
	}

	// Error cases
	@Test
	void testGetEventWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			publicService.getEvent(999L)
		);
		assertEquals("Event not found", exception.getMessage());
	}

	@Test
	void testGetEventNotPublished() {
		// Arrange
		Event draftEvent = eventRepository.save(Event.builder()
			.title("Draft Event")
			.description("Draft Description")
			.place("Test Place")
			.date(LocalDate.now().plusDays(7))
			.time(LocalTime.of(20, 0))
			.duration(Duration.ofHours(2))
			.maxNumberOfParticipants(100L)
			.state(State.fromString(DraftState.name, null))
			.build());

		publishedEvent.setState(State.fromString(DraftState.name, publishedEvent));

		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			publicService.getEvent(draftEvent.getId())
		);
		assertEquals("Event not found", exception.getMessage());
	}

	@Test
	void testGetPromoterWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			publicService.getPromoter(999L)
		);
		assertEquals("Promoter not found", exception.getMessage());
	}
}