package com.pareidolia.repository;

import com.pareidolia.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

	Page<Review> findAllByIdEvent(Long idEvent, PageRequest pageRequest);

	List<Review> findAllByIdEvent(Long idEvent);
}