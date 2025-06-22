package com.pareidolia.decorator.review;

import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// (agisce come uno dei ConcreteDecorators) estende ReviewDecorator e aggiunge comportamenti specifici.
public class HighlightDecorator extends ReviewDecorator {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private final AccountRepository accountRepository;

	public HighlightDecorator(ReviewComponent decoratedReview, AccountRepository accountRepository) {
		super(decoratedReview);
		this.accountRepository = accountRepository;
	}

	@Override
	public ReviewDTO apply() {
		ReviewDTO review = super.apply();
		// Verifica se l'account è di tipo Promoter
		if (getReferenceType(review.getIdConsumer()) == Account.Type.PROMOTER) {
			review.setAccountName("⭐️ [Promoter] " + review.getAccountName());
		}
		return review;
	}

	private Account.Type getReferenceType(Long idConsumer) {
		try {
			Account account = accountRepository.findById(idConsumer)
				.orElseThrow(() -> new IllegalArgumentException("Invalid Account"));

			return account.getReferenceType();
		} catch (IllegalArgumentException e) {
			// Gestisci l'eccezione se l'account non è trovato
			log.error("Error: ", e);
			// ^ System.err.println();
			return null;
		}
	}
}
