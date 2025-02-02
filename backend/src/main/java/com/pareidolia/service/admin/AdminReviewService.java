package com.pareidolia.service.admin;

import com.pareidolia.dto.AdminDTO;
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
public class AdminReviewService {
	private final AdminService adminService;
	private final ReviewValidator reviewValidator;
	private final EventRepository eventRepository;
	private final ReviewRepository reviewRepository;
	private final AccountRepository accountRepository;

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
		return reviews.map(ReviewMapper::entityToDTO);
	}

	public ReviewDTO create(ReviewDTO reviewDTO) {
		AdminDTO adminDTO = adminService.getData();

		Account account = accountRepository.findById(adminDTO.getId())
			.orElseThrow(() -> new IllegalArgumentException("Invalid Account"));

		reviewDTO.setId(null);
		if (reviewDTO.getIdConsumer() == null) {
			reviewDTO.setIdConsumer(adminDTO.getId());
		} else if (!reviewDTO.getIdConsumer().equals(adminDTO.getId())) {
			throw new IllegalArgumentException("Invalid id consumer");
		}

		reviewValidator.validateEventIsOver(reviewDTO.getIdEvent());
		reviewValidator.validateNewReviewFields(reviewDTO);

		Review newReview = ReviewMapper.dtoToEntity(reviewDTO);
		newReview.setAccount(account);

		newReview = reviewRepository.save(newReview);

		eventRepository.updateAverageScore(reviewDTO.getIdEvent());

		return ReviewMapper.entityToDTO(newReview);
	}

	public void delete(Long id) {
		if (id == null) {
			throw new IllegalArgumentException("Invalid ID");
		}

		Review review = reviewRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Review not found"));
		Long eventId = review.getIdEvent();

		reviewRepository.deleteById(id);
		eventRepository.updateAverageScore(eventId);
	}
}
