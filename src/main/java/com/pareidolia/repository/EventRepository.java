package com.pareidolia.repository;

import com.pareidolia.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
	@Query("""
			SELECT e
			FROM Event e
			INNER JOIN EventPromoterAssociation epa ON epa.idEvent = e.id
			WHERE epa.idPromoter = :idPromoter
		""")
	Page<Event> findAllByPromoterId(Long idPromoter, PageRequest pageRequest);
}