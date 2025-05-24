package com.pareidolia.mapper;

import com.pareidolia.dto.BookingDTO;
import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.dto.EventDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Booking;
import com.pareidolia.entity.Event;
import com.pareidolia.entity.PromoterInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.data.util.Pair;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {
	private Booking booking;
	private BookingDTO bookingDTO;
	private EventDTO eventDTO;
	private Event event;
	private Account consumerAccount;
	private ConsumerDTO consumerDTO;
	private LocalDateTime testTime;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		testTime = LocalDateTime.now();

		// Setup consumer account
		consumerAccount = new Account();
		consumerAccount.setId(1L);
		consumerAccount.setEmail("test@example.com");
		consumerAccount.setPassword("NewPassword123#");
		consumerAccount.setReferenceType(Account.Type.CONSUMER);

		// Setup consumer DTO
		ConsumerDTO consumerDTO = new ConsumerDTO();
		consumerDTO.setId(consumerAccount.getId());
		consumerDTO.setEmail("updated@test.com");
		consumerDTO.setName("Updated");
		consumerDTO.setSurname("Consumer");
		consumerDTO.setPhone("+39111222333");
		consumerDTO.setReferenceType(Account.Type.CONSUMER.name());

		// Setup event DTO
		event = new Event();
		event.setId(1L);
		event.setTitle("Test Event");

		// Setup event DTO
		eventDTO = new EventDTO();
		eventDTO.setId(1L);
		eventDTO.setTitle("Test Event");

		// Setup booking entity
		booking = new Booking();
		booking.setId(1L);
		booking.setIdEvent(2L);
		booking.setIdAccount(3L);
		booking.setCreationTime(testTime);
		booking.setEvent(event);

		// Setup booking DTO
		bookingDTO = new BookingDTO();
		bookingDTO.setId(1L);
		bookingDTO.setConsumer(consumerDTO);
		bookingDTO.setEvent(eventDTO);
		bookingDTO.setCreationTime(testTime);
	}

	@Test
	void testEntityToDTO() {
		// Setup
		List<Pair<Account, PromoterInfo>> promoters = new ArrayList<>();
		Long currentParticipants = 5L;

		// Test
		BookingDTO result = BookingMapper.entityToDTO(booking, consumerAccount, event, currentParticipants, promoters);

		// Verify
		assertNotNull(result);
		assertEquals(booking.getId(), result.getId());
		assertEquals(booking.getCreationTime(), result.getCreationTime());
	}

	@Test
	void testEntityToDTO_NullInput() {
		// Test
		BookingDTO result = BookingMapper.entityToDTO(null, consumerAccount, event, 0L, new ArrayList<>());

		// Verify
		assertNull(result);
	}

	@Test
	void testDtoToEntity() {
		// Test
		Booking result = BookingMapper.dtoToEntity(bookingDTO);

		// Verify
		assertNotNull(result);
		assertEquals(bookingDTO.getId(), result.getId());
		assertEquals(bookingDTO.getConsumer().getId(), result.getIdAccount());
		assertEquals(bookingDTO.getEvent().getId(), result.getIdEvent());
	}

	@Test
	void testDtoToEntity_NullInput() {
		// Test
		Booking result = BookingMapper.dtoToEntity(null);

		// Verify
		assertNull(result);
	}
} 