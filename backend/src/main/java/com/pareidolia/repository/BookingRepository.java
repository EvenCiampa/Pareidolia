package com.pareidolia.repository;

import com.pareidolia.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
	Page<Booking> findByIdEvent(Long idEvent, Pageable pageable);

	Optional<Booking> findByIdEventAndIdAccount(Long idEvent, Long idAccount);

	Long countByIdEvent(Long idEvent);
}
