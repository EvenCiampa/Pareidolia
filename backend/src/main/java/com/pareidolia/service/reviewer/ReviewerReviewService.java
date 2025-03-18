package com.pareidolia.service.reviewer;

import com.pareidolia.dto.ReviewDTO;
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
public class ReviewerReviewService {
	private final ReviewValidator reviewValidator;
	private final EventRepository eventRepository;
	private final ReviewRepository reviewRepository;
	private final AccountRepository accountRepository;

	/**
	 * Recupera tutte le recensioni associate a un evento specifico, paginandole.
	 * @param idEvent L'ID dell'evento per cui recuperare le recensioni.
	 * @return Page<ReviewDTO> Una pagina contenente le recensioni sotto forma di DTO.
	 */
	public Page<ReviewDTO> getEventReviews(Long idEvent, Integer page, Integer size) {
		if (idEvent == null) {
			throw new IllegalArgumentException("Invalid Event ID");
		}

		if (eventRepository.findById(idEvent).isEmpty()) {
			throw new IllegalArgumentException("Invalid Event ID");
		}
		reviewValidator.validateEventIsOver(idEvent);

		Page<Review> reviews = reviewRepository.findAllByIdEvent(idEvent,
			PageRequest.of(Math.max(0, Optional.ofNullable(page).orElse(0)),
				Math.max(10, Optional.ofNullable(size).orElse(10)),
				Sort.by(Sort.Order.desc("id")))
		);
		return reviews.map(review -> ReviewMapper.entityToDTO(review, accountRepository));
	}
}
