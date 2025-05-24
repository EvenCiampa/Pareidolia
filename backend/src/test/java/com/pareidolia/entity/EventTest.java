package com.pareidolia.entity;

import com.pareidolia.state.DraftState;
import com.pareidolia.state.State;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

	@Test
	void testNoArgsConstructor() {
		Event event = new Event();
		assertNotNull(event);
		assertNotNull(event.getState());
		assertEquals(DraftState.name, event.getState().getStateName());
	}

	@Test
	void testAllArgsConstructor() {
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.ofHours(2);
		PromoterInfo promoter = new PromoterInfo();

		Event event = new Event(1L, "Concert", "Amazing concert", "image.jpg",
			"Stadium", date, time, duration, 1000L, 4.5,
			State.fromString(DraftState.name, null), now, now,
			List.of(promoter));

		assertEquals(1L, event.getId());
		assertEquals("Concert", event.getTitle());
		assertEquals("Amazing concert", event.getDescription());
		assertEquals("image.jpg", event.getImage());
		assertEquals("Stadium", event.getPlace());
		assertEquals(date, event.getDate());
		assertEquals(time, event.getTime());
		assertEquals(duration, event.getDuration());
		assertEquals(1000L, event.getMaxNumberOfParticipants());
		assertEquals(4.5, event.getAverageScore());
		assertEquals(DraftState.name, event.getState().getStateName());
		assertEquals(now, event.getCreationTime());
		assertEquals(now, event.getLastUpdate());
		assertEquals(1, event.getPromoters().size());
	}

	@Test
	void testBuilder() {
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.ofHours(2);
		PromoterInfo promoter = new PromoterInfo();

		Event event = Event.builder()
			.id(1L)
			.title("Concert")
			.description("Amazing concert")
			.image("image.jpg")
			.place("Stadium")
			.date(date)
			.time(time)
			.duration(duration)
			.maxNumberOfParticipants(1000L)
			.averageScore(4.5)
			.state(State.fromString(DraftState.name, null))
			.creationTime(now)
			.lastUpdate(now)
			.promoters(List.of(promoter))
			.build();

		assertEquals(1L, event.getId());
		assertEquals("Concert", event.getTitle());
		assertEquals("Amazing concert", event.getDescription());
		assertEquals("image.jpg", event.getImage());
		assertEquals("Stadium", event.getPlace());
		assertEquals(date, event.getDate());
		assertEquals(time, event.getTime());
		assertEquals(duration, event.getDuration());
		assertEquals(1000L, event.getMaxNumberOfParticipants());
		assertEquals(4.5, event.getAverageScore());
		assertEquals(DraftState.name, event.getState().getStateName());
		assertEquals(now, event.getCreationTime());
		assertEquals(now, event.getLastUpdate());
		assertEquals(1, event.getPromoters().size());
	}

	@Test
	void testSetters() {
		Event event = new Event();
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.ofHours(2);
		PromoterInfo promoter = new PromoterInfo();

		event.setId(1L);
		event.setTitle("Concert");
		event.setDescription("Amazing concert");
		event.setImage("image.jpg");
		event.setPlace("Stadium");
		event.setDate(date);
		event.setTime(time);
		event.setDuration(duration);
		event.setMaxNumberOfParticipants(1000L);
		event.setAverageScore(4.5);
		event.setState(State.fromString(DraftState.name, null));
		event.setCreationTime(now);
		event.setLastUpdate(now);
		event.setPromoters(List.of(promoter));

		assertEquals(1L, event.getId());
		assertEquals("Concert", event.getTitle());
		assertEquals("Amazing concert", event.getDescription());
		assertEquals("image.jpg", event.getImage());
		assertEquals("Stadium", event.getPlace());
		assertEquals(date, event.getDate());
		assertEquals(time, event.getTime());
		assertEquals(duration, event.getDuration());
		assertEquals(1000L, event.getMaxNumberOfParticipants());
		assertEquals(4.5, event.getAverageScore());
		assertEquals(DraftState.name, event.getState().getStateName());
		assertEquals(now, event.getCreationTime());
		assertEquals(now, event.getLastUpdate());
		assertEquals(1, event.getPromoters().size());
	}

	@Test
	void testEqualsAndHashCode() {
		LocalDate date = LocalDate.now();
		LocalTime time = LocalTime.now();
		LocalDateTime now = LocalDateTime.now();
		Duration duration = Duration.ofHours(2);

		Event event1 = Event.builder()
			.id(1L)
			.title("Concert")
			.description("Amazing concert")
			.image("image.jpg")
			.place("Stadium")
			.date(date)
			.time(time)
			.duration(duration)
			.maxNumberOfParticipants(1000L)
			.averageScore(4.5)
			.state(State.fromString(DraftState.name, null))
			.creationTime(now)
			.lastUpdate(now)
			.promoters(new ArrayList<>())
			.build();

		Event event2 = Event.builder()
			.id(1L)
			.title("Concert")
			.description("Amazing concert")
			.image("image.jpg")
			.place("Stadium")
			.date(date)
			.time(time)
			.duration(duration)
			.maxNumberOfParticipants(1000L)
			.averageScore(4.5)
			.state(State.fromString(DraftState.name, null))
			.creationTime(now)
			.lastUpdate(now)
			.promoters(new ArrayList<>())
			.build();

		assertEquals(event1, event2);
		assertEquals(event1.hashCode(), event2.hashCode());

		// Test with different values
		Event event3 = Event.builder()
			.id(2L)
			.title("Different Concert")
			.build();

		assertNotEquals(event1, event3);
		assertNotEquals(event1.hashCode(), event3.hashCode());
	}

	@Test
	void testInitializeState() {
		Event event = new Event();
		event.initializeState(); // This would normally be called by @PostLoad
		assertNotNull(event.getState());
		assertEquals(DraftState.name, event.getState().getStateName());
	}
} 