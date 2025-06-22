package com.pareidolia.service.reviewer;

import com.pareidolia.dto.MessageDTO;
import com.pareidolia.dto.ReviewerDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.Message;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewerMessageServiceTest {

	@Mock
	private ReviewerService reviewerService;

	@Mock
	private MessageRepository messageRepository;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private EventRepository eventRepository;

	@InjectMocks
	private ReviewerMessageService reviewerMessageService;

	private Event testEvent;
	private Account testAccount;
	private Message testMessage;
	private ReviewerDTO testReviewerDTO;

	@BeforeEach
	void setUp() {
		testAccount = new Account();
		testAccount.setId(1L);
		testAccount.setEmail("test@example.com");
		testAccount.setReferenceType(Account.Type.REVIEWER);

		testReviewerDTO = new ReviewerDTO();
		testReviewerDTO.id = testAccount.getId();
		testReviewerDTO.email = testAccount.getEmail();

		testEvent = new Event();
		testEvent.setId(1L);
		testEvent.setTitle("Test Event");

		testMessage = new Message();
		testMessage.setId(1L);
		testMessage.setIdEvent(testEvent.getId());
		testMessage.setIdAccount(testAccount.getId());
		testMessage.setMessage("Test message content");
		testMessage.setCreationTime(LocalDateTime.now());
		testMessage.setAccount(testAccount);
	}

	@Test
	void testGetEventDraftMessagesSuccess() {
		List<Message> messages = Collections.singletonList(testMessage);
		Page<Message> messagePage = new PageImpl<>(messages);

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(messageRepository.findByIdEvent(eq(testEvent.getId()), any(PageRequest.class)))
			.thenReturn(messagePage);

		Page<MessageDTO> result = reviewerMessageService.getEventDraftMessages(testEvent.getId(), 0, 10);

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
		assertEquals(1, result.getContent().size());
		assertEquals(testMessage.getId(), result.getContent().get(0).id);
		assertEquals(testMessage.getMessage(), result.getContent().get(0).message);
	}

	@Test
	void testGetEventDraftMessagesNullEventId() {
		assertThrows(IllegalArgumentException.class,
			() -> reviewerMessageService.getEventDraftMessages(null, 0, 10));
	}

	@Test
	void testGetEventDraftMessagesInvalidEventId() {
		when(eventRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerMessageService.getEventDraftMessages(999L, 0, 10));
	}

	@Test
	void testGetEventDraftMessagesWithNullPageAndSize() {
		List<Message> messages = Collections.singletonList(testMessage);
		Page<Message> messagePage = new PageImpl<>(messages);

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(messageRepository.findByIdEvent(eq(testEvent.getId()), any(PageRequest.class)))
			.thenReturn(messagePage);

		Page<MessageDTO> result = reviewerMessageService.getEventDraftMessages(testEvent.getId(), null, null);

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
		assertEquals(1, result.getContent().size());
	}

	@Test
	void testGetEventDraftMessagesWithNegativePageAndSize() {
		List<Message> messages = Collections.singletonList(testMessage);
		Page<Message> messagePage = new PageImpl<>(messages);

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(messageRepository.findByIdEvent(eq(testEvent.getId()), any(PageRequest.class)))
			.thenReturn(messagePage);

		Page<MessageDTO> result = reviewerMessageService.getEventDraftMessages(testEvent.getId(), -1, -1);

		assertNotNull(result);
		assertFalse(result.getContent().isEmpty());
		assertEquals(1, result.getContent().size());
	}

	@Test
	void testCreateMessageSuccess() {
		String messageContent = "New test message";

		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(reviewerService.getData()).thenReturn(testReviewerDTO);
		when(accountRepository.findById(testReviewerDTO.id)).thenReturn(Optional.of(testAccount));
		when(messageRepository.save(any(Message.class))).thenReturn(testMessage);

		MessageDTO result = reviewerMessageService.create(testEvent.getId(), messageContent);

		assertNotNull(result);
		assertEquals(testMessage.getId(), result.id);
		assertEquals(testMessage.getMessage(), result.message);
	}

	@Test
	void testCreateMessageNullEventId() {
		assertThrows(IllegalArgumentException.class,
			() -> reviewerMessageService.create(null, "Test message"));
	}

	@Test
	void testCreateMessageInvalidEventId() {
		when(eventRepository.findById(999L)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerMessageService.create(999L, "Test message"));
	}

	@Test
	void testCreateMessageNullMessage() {
		assertThrows(IllegalArgumentException.class,
			() -> reviewerMessageService.create(testEvent.getId(), null));
	}

	@Test
	void testCreateMessageEmptyMessage() {
		assertThrows(IllegalArgumentException.class,
			() -> reviewerMessageService.create(testEvent.getId(), "   "));
	}

	@Test
	void testCreateMessageTooLong() {
		String longMessage = "a".repeat(1001);

		assertThrows(IllegalArgumentException.class,
			() -> reviewerMessageService.create(testEvent.getId(), longMessage));
	}

	@Test
	void testCreateMessageInvalidAccount() {
		when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
		when(reviewerService.getData()).thenReturn(testReviewerDTO);
		when(accountRepository.findById(testReviewerDTO.id)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
			() -> reviewerMessageService.create(testEvent.getId(), "Test message"));
	}
} 