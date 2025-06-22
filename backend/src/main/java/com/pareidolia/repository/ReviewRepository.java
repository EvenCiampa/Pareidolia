package com.pareidolia.repository;

import com.pareidolia.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	Page<Review> findAllByIdEvent(Long idEvent, Pageable pageable);

	List<Review> findAllByIdEvent(Long idEvent);

	Optional<Review> findByIdConsumerAndIdEvent(Long idConsumer, Long idEvent);
}