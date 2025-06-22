package com.pareidolia.service.reviewer;

import com.pareidolia.dto.BookingDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Booking;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.BookingRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.service.generic.PublicService;
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewerBookingServiceTest {

	@Mock
	private PublicService publicService;

	@Mock
	private EventRepository eventRepository;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private BookingRepository bookingRepository;

	@InjectMocks
	private ReviewerBookingService reviewerBookingService;

	private Booking testBooking;
	private Account testAccount;
	private Event testEvent;
	private PromoterInfo testPromoterInfo;
	private List<Pair<Account, PromoterInfo>> testPromoterPairs;

	@BeforeEach
	void setUp() {
		testAccount = new Account();
		testAccount.setId(1L);
		testAccount.setEmail("test@example.com");
		testAccount.setReferenceType(Account.Type.CONSUMER);

		testEvent = new Event();
		testEvent.setId(1L);
		testEvent.setTitle("Test Event");
		testEvent.setDate(LocalDate.now());
		testEvent.setTime(LocalTime.now());
		testEvent.setDuration(Duration.ofHours(2));
		testEvent.setMaxNumberOfParticipants(100L);
		testEvent.setPlace("Test Place");

		testBooking = new Booking();
		testBooking.setId(1L);
		testBooking.setIdAccount(testAccount.getId());
		testBooking.setIdEvent(testEvent.getId());
		testBooking.setCreationTime(LocalDateTime.now());

		Account promoterAccount = new Account();
		promoterAccount.setId(2L);
		promoterAccount.setReferenceType(Account.Type.PROMOTER);

		testPromoterInfo = new PromoterInfo();
		testPromoterInfo.setIdPromoter(promoterAccount.getId());

		testPromoterPairs = Collections.singletonList(Pair.of(promoterAccount, testPromoterInfo));
	}

	@Test
	void testGetBookingSuccess() {
		when(bookingRepository.findById(testBooking.getId())).thenReturn(Optional.of(testBooking));
		when(accountRepository.findById(testBooking.getIdAccount())).thenReturn(Optional.of(testAccount));
		when(eventRepository.findById(testBooking.getIdEvent())).thenReturn(Optional.of(testEvent));
		when(bookingRepository.countByIdEvent(testEvent.getId())).thenReturn(50L);
		when(publicService.findPromotersByEventId(testEvent.getId())).thenReturn(testPromoterPairs);

		BookingDTO result = reviewerBookingService.getBooking(testBooking.getId());

		assertNotNull(result);
		assertEquals(testBooking.getId(), result.id);
		assertNotNull(result.event);
		assertNotNull(result.consumer);
	}

	@Test
	void testGetBookingInvalidId() {
		when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerBookingService.getBooking(999L));
	}

	@Test
	void testGetBookingInvalidAccountId() {
		when(bookingRepository.findById(testBooking.getId())).thenReturn(Optional.of(testBooking));
		when(accountRepository.findById(testBooking.getIdAccount())).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerBookingService.getBooking(testBooking.getId()));
	}

	@Test
	void testGetBookingInvalidEventId() {
		when(bookingRepository.findById(testBooking.getId())).thenReturn(Optional.of(testBooking));
		when(accountRepository.findById(testBooking.getIdAccount())).thenReturn(Optional.of(testAccount));
		when(eventRepository.findById(testBooking.getIdEvent())).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerBookingService.getBooking(testBooking.getId()));
	}

	@Test
	void testGetBookingsSuccess() {
		List<Booking> bookings = Collections.singletonList(testBooking);
		Page<Booking> bookingPage = new PageImpl<>(bookings);

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(bookingRepository.countByIdEvent(testEvent.getId())).thenReturn(50L);
		when(bookingRepository.findByIdEvent(eq(testEvent.getId()), any(PageRequest.class)))
			.thenReturn(bookingPage);
		when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
		when(publicService.findPromotersByEventId(testEvent.getId())).thenReturn(testPromoterPairs);

		Page<BookingDTO> result = reviewerBookingService.getBookings(testEvent.getId(), 0, 20);

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
		assertEquals(1, result.getContent().size());
		assertEquals(testBooking.getId(), result.getContent().get(0).id);
	}

	@Test
	void testGetBookingsInvalidEventId() {
		when(eventRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerBookingService.getBookings(999L, 0, 20));
	}

	@Test
	void testGetBookingsWithNullPageAndSize() {
		List<Booking> bookings = Collections.singletonList(testBooking);
		Page<Booking> bookingPage = new PageImpl<>(bookings);

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(bookingRepository.countByIdEvent(testEvent.getId())).thenReturn(50L);
		when(bookingRepository.findByIdEvent(eq(testEvent.getId()), any(PageRequest.class)))
			.thenReturn(bookingPage);
		when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
		when(publicService.findPromotersByEventId(testEvent.getId())).thenReturn(testPromoterPairs);

		Page<BookingDTO> result = reviewerBookingService.getBookings(testEvent.getId(), null, null);

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
	}

	@Test
	void testGetBookingsWithNegativePageAndSize() {
		List<Booking> bookings = Collections.singletonList(testBooking);
		Page<Booking> bookingPage = new PageImpl<>(bookings);

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(bookingRepository.countByIdEvent(testEvent.getId())).thenReturn(50L);
		when(bookingRepository.findByIdEvent(eq(testEvent.getId()), any(PageRequest.class)))
			.thenReturn(bookingPage);
		when(accountRepository.findById(testAccount.getId())).thenReturn(Optional.of(testAccount));
		when(publicService.findPromotersByEventId(testEvent.getId())).thenReturn(testPromoterPairs);

		Page<BookingDTO> result = reviewerBookingService.getBookings(testEvent.getId(), -1, -1);

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
	}
} 