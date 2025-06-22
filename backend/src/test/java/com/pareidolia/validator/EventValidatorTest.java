package com.pareidolia.validator;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Event;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EventValidatorTest {

	@Mock
	private EventRepository eventRepository;

	@Mock
	private AccountValidator accountValidator;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private EventPromoterAssociationRepository eventPromoterAssociationRepository;

	@Mock
	private Event mockEvent;

	@Mock
	private PromoterDTO mockPromoterDTO;

	@InjectMocks
	private EventValidator eventValidator;

	private EventDTO validEventDTO;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		validEventDTO = new EventDTO();
		validEventDTO.setId(1L);
		validEventDTO.setTitle("Test Event");
		validEventDTO.setDescription("Test Description");
		validEventDTO.setPlace("Test Place");
		validEventDTO.setDate(LocalDate.now().plusDays(1));
		validEventDTO.setTime(LocalTime.of(14, 0));
		validEventDTO.setDuration(Duration.ofHours(2));
		validEventDTO.setMaxNumberOfParticipants(100L);
		validEventDTO.setPromoters(List.of(mockPromoterDTO));

		when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
	}

	@Test
	void testGetEventAndValidateUpdate_Success() {
		// Test
		Event result = eventValidator.getEventAndValidateUpdate(validEventDTO);

		// Verify
		assertNotNull(result);
		verify(eventRepository).findById(1L);
		verify(accountValidator).getPromoterAndValidate(any());
	}

	@Test
	void testGetEventAndValidateUpdate_EventNotFound() {
		// Setup
		when(eventRepository.findById(any())).thenReturn(Optional.empty());

		// Test
		assertThrows(IllegalArgumentException.class,
			() -> eventValidator.getEventAndValidateUpdate(validEventDTO),
			"Event not found");
	}

	@Test
	void testValidateEventDate_Success() {
		LocalDate futureDate = LocalDate.now().plusDays(1);
		LocalTime validTime = LocalTime.now();

		// Should not throw exception
		eventValidator.validateEventDate(futureDate, validTime);
	}

	@Test
	void testValidateEventDate_PastDateTime() {
		LocalDate pastDate = LocalDate.now().minusDays(1);
		LocalTime pastTime = LocalTime.now().minusHours(1);

		assertThrows(IllegalArgumentException.class,
			() -> eventValidator.validateEventDate(pastDate, pastTime),
			"Event date must be in the future.");
	}

	@Test
	void testValidateEventDate_NullTime() {
		LocalDate validDate = LocalDate.now().plusDays(1);

		assertThrows(IllegalArgumentException.class,
			() -> eventValidator.validateEventDate(validDate, null),
			"Event time must be specified.");
	}

	@Test
	void testValidateEventDate_NullDate() {
		LocalTime validTime = LocalTime.now();

		assertThrows(IllegalArgumentException.class,
			() -> eventValidator.validateEventDate(null, validTime),
			"Event date must be specified.");
	}

	@Test
	void testGetEventAndValidateUpdate_InvalidTitle() {
		validEventDTO.setTitle("");
		assertThrows(IllegalArgumentException.class,
			() -> eventValidator.getEventAndValidateUpdate(validEventDTO),
			"Event title must not be empty.");
	}

	@Test
	void testGetEventAndValidateUpdate_InvalidDescription() {
		validEventDTO.setDescription("");
		assertThrows(IllegalArgumentException.class,
			() -> eventValidator.getEventAndValidateUpdate(validEventDTO),
			"Event description must not be empty.");
	}

	@Test
	void testGetEventAndValidateUpdate_InvalidPlace() {
		validEventDTO.setPlace("");
		assertThrows(IllegalArgumentException.class,
			() -> eventValidator.getEventAndValidateUpdate(validEventDTO),
			"Event place must not be empty.");
	}

	@Test
	void testGetEventAndValidateUpdate_InvalidDuration() {
		validEventDTO.setDuration(Duration.ZERO);
		assertThrows(IllegalArgumentException.class,
			() -> eventValidator.getEventAndValidateUpdate(validEventDTO),
			"Event duration must be positive.");
	}

	@Test
	void testGetEventAndValidateUpdate_InvalidMaxParticipants() {
		validEventDTO.setMaxNumberOfParticipants(0L);
		assertThrows(IllegalArgumentException.class,
			() -> eventValidator.getEventAndValidateUpdate(validEventDTO),
			"Maximum number of participants must be greater than zero.");
	}
} 