package com.pareidolia.service;

import com.pareidolia.configuration.mail.CustomMailSender;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Event;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.strategy.email.event.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public class SendEmailForEvent implements Runnable {
	final EmailEventType emailEventType;
	final Event event;
	final CustomMailSender mailSender;
	final AccountRepository accountRepository;

	public SendEmailForEvent(EmailEventType emailEventType, Event event, CustomMailSender mailSender, AccountRepository accountRepository) {
		this.emailEventType = emailEventType;
		this.event = event;
		this.mailSender = mailSender;
		this.accountRepository = accountRepository;
	}

	@Override
	public void run() {
		// Map per associare ogni tipo di account alla sua strategia di email
		Map<Account.Type, EmailContentStrategy> strategyMap = Map.of(
			Account.Type.CONSUMER, new ConsumerInvitationStrategy(),
			Account.Type.PROMOTER, new PromoterInvitationStrategy(),
			Account.Type.REVIEWER, new ReviewerConfirmationStrategy()
		);

		// Lista dei tipi di account da notificare
		List<Account.Type> accountTypesToNotify = List.of(
			Account.Type.CONSUMER,
			Account.Type.PROMOTER,
			Account.Type.REVIEWER
		);

		// Notifica tutti gli account non-admin usando la strategia appropriata
		accountTypesToNotify.forEach(accountType -> {
			EmailContentStrategy strategy = strategyMap.get(accountType);
			accountRepository.findAllByReferenceType(accountType, Pageable.unpaged())
				.forEach(account -> {
					String subject = strategy.generateSubject(emailEventType, event);
					String body = strategy.generateBody(emailEventType, event);
					mailSender.sendMail(account.getEmail(), subject, body, false, "sendEventInvitation");
				});
		});
	}
}
