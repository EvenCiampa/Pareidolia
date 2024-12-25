package com.pareidolia.validator;

import com.pareidolia.dto.EventDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventPromoterAssociationRepository;
import com.pareidolia.repository.EventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class EventValidator {
	public final EventRepository eventRepository;
	public final AccountValidator accountValidator;
	public final AccountRepository accountRepository;
	public final EventPromoterAssociationRepository eventPromoterAssociationRepository;

	private void titleValidation(String title) {
		if (title == null || title.trim().isEmpty()) {
			throw new IllegalArgumentException("Event title must not be empty.");
		}
	}

	private void descriptionValidation(String description) {
		if (description == null || description.trim().isEmpty()) {
			throw new IllegalArgumentException("Event description must not be empty.");
		}
	}

	private void placeValidation(String place) {
		if (place == null || place.trim().isEmpty()) {
			throw new IllegalArgumentException("Event place must not be empty.");
		}
	}

	private void dateValidation(LocalDate date) {
		if (date == null) {
			throw new IllegalArgumentException("Event date must be specified.");
		}
		if (date.isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("Event date must be in the future.");
		}
	}

	private void timeValidation(LocalTime time) {
		if (time == null) {
			throw new IllegalArgumentException("Event time must be specified.");
		}
	}

	private void durationValidation(Duration duration) {
		if (duration == null || duration.isNegative() || duration.isZero()) {
			throw new IllegalArgumentException("Event duration must be positive.");
		}
	}

	private void maxNumberOfParticipantsValidation(Long maxNumberOfParticipants) {
		if (maxNumberOfParticipants != null && maxNumberOfParticipants <= 0) {
			throw new IllegalArgumentException("Maximum number of participants must be greater than zero.");
		}
	}

	public void validateEventDate(LocalDate date, LocalTime time) {
		timeValidation(time);
		dateValidation(date);

		LocalDateTime eventDateTime = LocalDateTime.of(date, time);
		if (eventDateTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Event date and time must be in the future.");
		}
	}

	private Account validateAccountExists(Long accountId) {
		return accountRepository.findById(accountId)
			.orElseThrow(() -> new IllegalArgumentException("Account not found."));
	}

	public Event getEventAndValidateUpdate(EventDTO eventDTO) {
		Event event = eventRepository.findById(eventDTO.getId())
			.orElseThrow(() -> new IllegalArgumentException("Event not found"));

		titleValidation(eventDTO.getTitle());
		descriptionValidation(eventDTO.getDescription());
		placeValidation(eventDTO.getPlace());
		validateEventDate(eventDTO.getDate(), eventDTO.getTime());
		durationValidation(eventDTO.getDuration());
		maxNumberOfParticipantsValidation(eventDTO.getMaxNumberOfParticipants());
		eventDTO.getPromoters().forEach(accountValidator::getPromoterAndValidateUpdate);
		return event;
	}
}
