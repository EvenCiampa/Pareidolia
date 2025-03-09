package com.pareidolia.service.admin;

import com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils;
import com.pareidolia.dto.BookingDTO;
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
public class AdminBookingServiceTest {

	private final String adminEmail = "admin@test.com";
	private final String adminPassword = "TestPassword123#";
	private final String promoterEmail = "promoter@test.com";
	private final String promoterPassword = "TestPassword123#";
	private final String consumerEmail = "test@test.com";
	private final String consumerPassword = "TestPassword123#";
	@Autowired
	private AdminBookingService adminBookingService;
	@Autowired
	private AccountRepository accountRepository;
	@Autowired
	private PromoterInfoRepository promoterInfoRepository;
	@Autowired
	private EventRepository eventRepository;
	@Autowired
	private BookingRepository bookingRepository;
	@Autowired
	private EventPromoterAssociationRepository eventPromoterAssociationRepository;
	private Account adminAccount;
	private Account promoterAccount;
	private Account consumerAccount;
	private Event testEvent;
	private Booking testBooking;

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

		promoterInfoRepository.save(PromoterInfo.builder()
			.idPromoter(promoterAccount.getId())
			.presentation("Test presentation")
			.build());

		// Create consumer account
		consumerAccount = accountRepository.save(Account.builder()
			.email(consumerEmail)
			.password(DigestUtils.sha3_256Hex(consumerPassword))
			.name("Test")
			.surname("Consumer")
			.phone("+39555666777")
			.referenceType(Account.Type.CONSUMER)
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

		// Create test booking
		testBooking = bookingRepository.save(Booking.builder()
			.idEvent(testEvent.getId())
			.idAccount(consumerAccount.getId())
			.build());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetBooking() {
		// Act
		BookingDTO bookingDTO = adminBookingService.getBooking(testBooking.getId());

		// Assert
		assertNotNull(bookingDTO);
		assertEquals(testBooking.getId(), bookingDTO.getId());
		assertEquals(consumerAccount.getId(), bookingDTO.getConsumer().getId());
		assertEquals(testEvent.getId(), bookingDTO.getEvent().getId());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetBookings() {
		// Act
		Page<BookingDTO> bookings = adminBookingService.getBookings(testEvent.getId(), 0, 20);

		// Assert
		assertNotNull(bookings);
		assertFalse(bookings.isEmpty());
		assertEquals(1, bookings.getTotalElements());
		assertEquals(testBooking.getId(), bookings.getContent().get(0).getId());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDelete() {
		// Act
		adminBookingService.delete(testBooking.getId());

		// Assert
		assertTrue(bookingRepository.findById(testBooking.getId()).isEmpty());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetBookingWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminBookingService.getBooking(999L)
		);
		assertEquals("Invalid booking ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testGetBookingsWithInvalidEventId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminBookingService.getBookings(999L, 0, 20)
		);
		assertEquals("Invalid Event ID", exception.getMessage());
	}

	@Test
	@WithMockUser(username = adminEmail, authorities = {"ADMIN"})
	void testDeleteWithInvalidId() {
		// Act & Assert
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
			adminBookingService.delete(999L)
		);
		assertEquals("Invalid booking ID", exception.getMessage());
	}
}