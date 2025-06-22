package com.pareidolia.service.consumer;

import com.pareidolia.dto.ConsumerDTO;
import com.pareidolia.dto.ReviewDTO;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.Review;
import com.pareidolia.mapper.ReviewMapper;
import com.pareidolia.repository.AccountRepository;
import com.pareidolia.repository.EventRepository;
import com.pareidolia.repository.ReviewRepository;
import com.pareidolia.validator.ReviewValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ConsumerReviewService {
	private final ReviewValidator reviewValidator;
	private final EventRepository eventRepository;
	private final ConsumerService consumerService;
	private final ReviewRepository reviewRepository;
	private final AccountRepository accountRepository;

	/**
	 * Recupera tutte le recensioni associate a un evento specifico, paginandole.
	 * Verifica che l'evento sia concluso prima di consentire l'accesso alle recensioni.
	 * @param idEvent L'ID dell'evento per cui recuperare le recensioni.
	 * @return Page<ReviewDTO> Una pagina contenente le recensioni sotto forma di DTO.
	 * @throws IllegalArgumentException Se l'ID dell'evento non è valido o se l'evento non è concluso.
	 */
	public Page<ReviewDTO> getEventReviews(Long idEvent, Integer page, Integer size) {
		if (idEvent == null) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		// Verifica la presenza dell'evento e che sia finito
		if (eventRepository.findById(idEvent).isEmpty()) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		reviewValidator.validateEventIsOver(idEvent);

		// ricerca messaggi relativi a un evento e paginali
		Page<Review> reviews = reviewRepository.findAllByIdEvent(idEvent,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)), Math.max(10, Optional.ofNullable(size).orElse(10)), Sort.by(Sort.Order.desc("id")))
		);
		return reviews.map(review -> ReviewMapper.entityToDTO(review, accountRepository));
	}

	/**
	 * Crea una nuova recensione per un evento, verificando che l'evento sia concluso e che il consumatore sia autorizzato a lasciare una recensione.
	 * Imposta il consumatore che lascia la recensione in base ai dati dell'utente autenticato.
	 * @param reviewDTO DTO contenente i dati della recensione da creare.
	 * @return ReviewDTO Il DTO della recensione appena creata.
	 */
	public ReviewDTO create(ReviewDTO reviewDTO) {
		ConsumerDTO consumerDTO = consumerService.getData();

		Account account = accountRepository.findById(consumerDTO.getId())
			.orElseThrow(() -> new IllegalArgumentException("Invalid Account"));

		reviewDTO.setId(null);
		if (reviewDTO.getIdConsumer() == null) {
			reviewDTO.setIdConsumer(consumerDTO.getId());
		} else if (!reviewDTO.getIdConsumer().equals(consumerDTO.getId())) {
			throw new IllegalArgumentException("Invalid id consumer");
		}

		reviewValidator.validateEventIsOver(reviewDTO.getIdEvent());
		reviewValidator.validateNewReviewFields(reviewDTO);

		Review newReview = ReviewMapper.dtoToEntity(reviewDTO);
		newReview.setAccount(account);

		newReview = reviewRepository.save(newReview);

		eventRepository.updateAverageScore(reviewDTO.getIdEvent());

		return ReviewMapper.entityToDTO(newReview, accountRepository);
	}
}
