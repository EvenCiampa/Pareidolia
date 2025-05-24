package com.pareidolia.validator;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.dto.PromoterDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.EventPromoterAssociation;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.state.DraftState;
import com.pareidolia.state.PublishedState;
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
import static org.mockito.Mockito.*;

class EventDraftValidatorTest {

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
	private Account mockAccount;

	@Mock
	private DraftState mockDraftState;

	@Mock
	private PublishedState mockPublishedState;

	@Mock
	private PromoterDTO mockPromoterDTO;

	@InjectMocks
	private EventDraftValidator eventDraftValidator;

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
		when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));
		when(mockAccount.getReferenceType()).thenReturn(Account.Type.PROMOTER);
	}

	@Test
	void testCreateEventDraftValidator_Success() {
		// Test
		eventDraftValidator.createEventDraftValidator(validEventDTO);
		// No exception should be thrown
	}

	@Test
	void testValidateAddPromoterToEventDraft_Success() {
		// Setup
		when(mockEvent.getState()).thenReturn(mockDraftState);
		when(mockDraftState.canEdit()).thenReturn(true);
		when(eventPromoterAssociationRepository.findByIdEventAndIdPromoter(any(), any())).thenReturn(Optional.empty());

		// Test
		eventDraftValidator.validateAddPromoterToEventDraft(1L, 1L);

		// Verify
		verify(eventRepository).findById(1L);
		verify(accountRepository).findById(1L);
		verify(accountValidator).accountTypeValidation(Account.Type.PROMOTER, Account.Type.PROMOTER);
	}

	@Test
	void testValidateAddPromoterToEventDraft_EventNotFound() {
		// Setup
		when(eventRepository.findById(any())).thenReturn(Optional.empty());

		// Test
		assertThrows(IllegalArgumentException.class,
			() -> eventDraftValidator.validateAddPromoterToEventDraft(1L, 1L),
			"Invalid EventDraft ID");
	}

	@Test
	void testValidateAddPromoterToEventDraft_AccountNotFound() {
		// Setup
		when(accountRepository.findById(any())).thenReturn(Optional.empty());

		// Test
		assertThrows(IllegalArgumentException.class,
			() -> eventDraftValidator.validateAddPromoterToEventDraft(1L, 1L),
			"Account not found.");
	}

	@Test
	void testValidateAddPromoterToEventDraft_AlreadyAssociated() {
		// Setup
		when(mockEvent.getState()).thenReturn(mockDraftState);
		when(mockDraftState.canEdit()).thenReturn(true);
		when(eventPromoterAssociationRepository.findByIdEventAndIdPromoter(any(), any())).thenReturn(Optional.of(new EventPromoterAssociation()));

		// Test
		assertThrows(IllegalArgumentException.class,
			() -> eventDraftValidator.validateAddPromoterToEventDraft(1L, 1L),
			"Promoter is already associated with this event.");
	}

	@Test
	void testValidateEditable_Success() {
		// Setup
		when(mockEvent.getState()).thenReturn(mockDraftState);
		when(mockDraftState.canEdit()).thenReturn(true);

		// Test
		eventDraftValidator.validateEditable(mockEvent);
		// No exception should be thrown
	}

	@Test
	void testValidateEditable_NotEditable() {
		// Setup
		when(mockEvent.getState()).thenReturn(mockPublishedState);
		when(mockPublishedState.canEdit()).thenReturn(false);
		when(mockPublishedState.getStateName()).thenReturn("PUBLISHED");

		// Test
		assertThrows(IllegalArgumentException.class,
			() -> eventDraftValidator.validateEditable(mockEvent),
			"Event cannot be edited in the current state: PUBLISHED");
	}

	@Test
	void testGetEventDraftAndValidateUpdate_Success() {
		// Setup
		when(mockEvent.getState()).thenReturn(mockDraftState);
		when(mockDraftState.canEdit()).thenReturn(true);
		when(mockEvent.getId()).thenReturn(1L);
		when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
		when(mockPromoterDTO.getId()).thenReturn(1L);
		when(accountRepository.findById(1L)).thenReturn(Optional.of(mockAccount));
		when(eventPromoterAssociationRepository.findByIdEventAndIdPromoter(any(), any())).thenReturn(Optional.empty());
		when(eventPromoterAssociationRepository.findPromotersByIdEvent(any())).thenReturn(List.of());

		// Test
		Event result = eventDraftValidator.getEventDraftAndValidateUpdate(validEventDTO);

		// Verify
		assertNotNull(result);
		verify(eventRepository, atLeast(1)).findById(1L);
		verify(mockEvent, times(2)).getState();
		verify(mockDraftState, times(2)).canEdit();
	}

	@Test
	void testGetEventDraftAndValidateUpdate_NoPromoters() {
		// Setup
		validEventDTO.setPromoters(null);
		when(mockEvent.getState()).thenReturn(mockDraftState);
		when(mockDraftState.canEdit()).thenReturn(true);

		// Test
		assertThrows(IllegalArgumentException.class,
			() -> eventDraftValidator.getEventDraftAndValidateUpdate(validEventDTO),
			"Promoters must not be empty.");
	}

	@Test
	void testCreateEventDraftValidatorWithPromoter_Success() {
		// Test
		eventDraftValidator.createEventDraftValidatorWithPromoter(validEventDTO);

		// Verify
		verify(accountValidator).getPromoterAndValidate(any());
	}

	@Test
	void testCreateEventDraftValidatorWithPromoter_NoPromoters() {
		// Setup
		validEventDTO.setPromoters(null);

		// Test
		assertThrows(IllegalArgumentException.class,
			() -> eventDraftValidator.createEventDraftValidatorWithPromoter(validEventDTO),
			"Event promoters must not be empty.");
	}
} 