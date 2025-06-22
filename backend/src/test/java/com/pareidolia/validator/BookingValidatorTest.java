package com.pareidolia.validator;

import com.pareidolia.entity.Account;
import com.pareidolia.entity.Booking;
import com.pareidolia.entity.Event;
import com.pareidolia.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class BookingValidatorTest {

	@Mock
	private BookingRepository bookingRepository;

	@Mock
	private Account mockAccount;

	@Mock
	private Event mockEvent;

	@Mock
	private Booking mockBooking;

	@InjectMocks
	private BookingValidator bookingValidator;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// Setup common mock behavior
		when(mockAccount.getId()).thenReturn(1L);
		when(mockEvent.getId()).thenReturn(1L);
		when(mockEvent.getMaxNumberOfParticipants()).thenReturn(10L);
	}

	@Test
	void testCreateBookingValidator_Success() {
		// Setup
		when(bookingRepository.findByIdEventAndIdAccount(any(), any())).thenReturn(Optional.empty());
		when(bookingRepository.countByIdEvent(any())).thenReturn(5L);

		// Test - should not throw any exception
		bookingValidator.createBookingValidator(mockAccount, mockEvent);
	}

	@Test
	void testCreateBookingValidator_BookingAlreadyExists() {
		// Setup
		when(bookingRepository.findByIdEventAndIdAccount(any(), any())).thenReturn(Optional.of(mockBooking));

		// Test
		assertThrows(IllegalArgumentException.class,
			() -> bookingValidator.createBookingValidator(mockAccount, mockEvent),
			"Booking for this event already exists");
	}

	@Test
	void testCreateBookingValidator_EventFullyBooked() {
		// Setup
		when(bookingRepository.findByIdEventAndIdAccount(any(), any())).thenReturn(Optional.empty());
		when(bookingRepository.countByIdEvent(any())).thenReturn(10L); // Equal to max participants

		// Test
		assertThrows(IllegalArgumentException.class,
			() -> bookingValidator.createBookingValidator(mockAccount, mockEvent),
			"Fully booked event");
	}

	@Test
	void testCreateBookingValidator_EventOverBooked() {
		// Setup
		when(bookingRepository.findByIdEventAndIdAccount(any(), any())).thenReturn(Optional.empty());
		when(bookingRepository.countByIdEvent(any())).thenReturn(11L); // More than max participants

		// Test
		assertThrows(IllegalArgumentException.class,
			() -> bookingValidator.createBookingValidator(mockAccount, mockEvent),
			"Fully booked event");
	}
} 