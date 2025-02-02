package com.pareidolia.repository;

import com.pareidolia.entity.Event;
import com.pareidolia.repository.model.EventWithInfo;
import com.pareidolia.repository.model.EventWithInfoForAccount;
import org.hibernate.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
	@Query(value = """
			SELECT new com.pareidolia.repository.model.EventWithInfoForAccount(
				e,
				(SELECT COUNT(b) > 0 FROM Booking b INNER JOIN Account a ON a.id = b.idAccount WHERE b.idEvent = e.id AND a.id = :accountId),
				(SELECT COUNT(b) FROM Booking b WHERE b.idEvent = e.id)
			)
			FROM Event e
			INNER JOIN EventPromoterAssociation epa ON epa.idEvent = e.id
			WHERE epa.idPromoter = :idPromoter AND e.state = :state
		""", countQuery = """
			SELECT COUNT(e)
			FROM Event e
			INNER JOIN EventPromoterAssociation epa ON epa.idEvent = e.id
			WHERE epa.idPromoter = :idPromoter AND e.state = :state
		""")
	Page<EventWithInfoForAccount> findAllByAccountIdAndStateAndPromoterId(@Param("accountId") Long accountId, Event.EventState state, Long idPromoter, Pageable pageable);

	@Query("""
			SELECT new com.pareidolia.repository.model.EventWithInfo(e, (SELECT COUNT(*) FROM Booking b WHERE b.idEvent = e.id))
			FROM Event e
			INNER JOIN EventPromoterAssociation epa ON epa.idEvent = e.id
			WHERE epa.idPromoter = :idPromoter AND e.state = :state
		""")
	Page<EventWithInfo> findAllByStateAndPromoterId(Event.EventState state, Long idPromoter, Pageable pageable);

	@Query(value = """
			SELECT new com.pareidolia.repository.model.EventWithInfoForAccount(
				e,
				(SELECT COUNT(b) > 0 FROM Booking b INNER JOIN Account a ON a.id = b.idAccount WHERE b.idEvent = e.id AND a.id = :accountId),
				(SELECT COUNT(b) FROM Booking b WHERE b.idEvent = e.id)
			)
			FROM Event e
			INNER JOIN EventPromoterAssociation epa ON epa.idEvent = e.id
			WHERE epa.idPromoter = :idPromoter
		""", countQuery = """
			SELECT COUNT(e)
			FROM Event e
			INNER JOIN EventPromoterAssociation epa ON epa.idEvent = e.id
			WHERE epa.idPromoter = :idPromoter
		""")
	Page<EventWithInfoForAccount> findAllByAccountIdAndPromoterId(@Param("accountId") Long accountId, Long idPromoter, Pageable pageable);

	@Query(value = """
			SELECT new com.pareidolia.repository.model.EventWithInfoForAccount(
				e,
				(SELECT COUNT(b) > 0 FROM Booking b INNER JOIN Account a ON a.id = b.idAccount WHERE b.idEvent = e.id AND a.id = :accountId),
				(SELECT COUNT(b) FROM Booking b WHERE b.idEvent = e.id)
			)
			FROM Event e
			WHERE e.state = :state
		""", countQuery = """
			SELECT COUNT(e)
			FROM Event e
			WHERE e.state = :state
		""")
	Page<EventWithInfoForAccount> findAllByAccountIdAndState(@Param("accountId") Long accountId, Event.EventState state, Pageable pageable);

	@Query("""
			SELECT new com.pareidolia.repository.model.EventWithInfo(e, (SELECT COUNT(*) FROM Booking b WHERE b.idEvent = e.id))
			FROM Event e
			WHERE e.state = :state
		""")
	Page<EventWithInfo> findAllByState(Event.EventState state, Pageable pageable);

	@Query(value = """
			SELECT new com.pareidolia.repository.model.EventWithInfoForAccount(
				e,
				(SELECT COUNT(b) > 0 FROM Booking b INNER JOIN Account a ON a.id = b.idAccount WHERE b.idEvent = e.id AND a.id = :accountId),
				(SELECT COUNT(b) FROM Booking b WHERE b.idEvent = e.id)
			)
			FROM Event e
		""", countQuery = """
			SELECT COUNT(e)
			FROM Event e
		""")
	Page<EventWithInfoForAccount> findAllByAccountIdWithCount(@Param("accountId") Long accountId, Pageable pageable);

	@Modifying
	@Query("""
			UPDATE Event e
			SET e.averageScore = (SELECT AVG(r.score) FROM Review r WHERE r.idEvent = e.id)
			WHERE e.id = :id
		""")
	void updateAverageScore(Long id);
}