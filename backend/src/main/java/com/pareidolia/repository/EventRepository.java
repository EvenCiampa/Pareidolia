package com.pareidolia.repository;

import com.pareidolia.entity.Event;
import com.pareidolia.repository.model.EventWithInfo;
import com.pareidolia.repository.model.EventWithInfoForAccount;
import com.pareidolia.state.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, CustomEventRepository {
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
	Page<EventWithInfoForAccount> findAllByAccountIdAndStateAndPromoterId(@Param("accountId") Long accountId, State state, Long idPromoter, Pageable pageable);

	@Query("""
			SELECT new com.pareidolia.repository.model.EventWithInfo(e, (SELECT COUNT(*) FROM Booking b WHERE b.idEvent = e.id))
			FROM Event e
			INNER JOIN EventPromoterAssociation epa ON epa.idEvent = e.id
			WHERE epa.idPromoter = :idPromoter AND e.state = :state
		""")
	Page<EventWithInfo> findAllByStateAndPromoterId(State state, Long idPromoter, Pageable pageable);

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
	Page<EventWithInfoForAccount> findAllByAccountIdAndState(@Param("accountId") Long accountId, State state, Pageable pageable);

	@Query("""
			SELECT new com.pareidolia.repository.model.EventWithInfo(e, (SELECT COUNT(*) FROM Booking b WHERE b.idEvent = e.id))
			FROM Event e
			WHERE e.state = :state
		""")
	Page<EventWithInfo> findAllByState(State state, Pageable pageable);

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

interface CustomEventRepository {
	Page<EventWithInfoForAccount> findAllByAccountIdAndStateAndPromoterId(Long accountId, String state, Long idPromoter, Pageable pageable);

	Page<EventWithInfo> findAllByStateAndPromoterId(String state, Long idPromoter, Pageable pageable);

	Page<EventWithInfoForAccount> findAllByAccountIdAndState(Long accountId, String state, Pageable pageable);

	Page<EventWithInfo> findAllByState(String state, Pageable pageable);
}

@Repository
class CustomEventRepositoryImpl implements CustomEventRepository {
	final EventRepository eventRepository;

	public CustomEventRepositoryImpl(@Autowired @Lazy EventRepository eventRepository) {
		this.eventRepository = eventRepository;
	}

	public Page<EventWithInfoForAccount> findAllByAccountIdAndStateAndPromoterId(Long accountId, String state, Long idPromoter, Pageable pageable) {
		return eventRepository.findAllByAccountIdAndStateAndPromoterId(accountId, State.fromString(state, null), idPromoter, pageable);
	}

	public Page<EventWithInfo> findAllByStateAndPromoterId(String state, Long idPromoter, Pageable pageable) {
		return eventRepository.findAllByStateAndPromoterId(State.fromString(state, null), idPromoter, pageable);
	}

	public Page<EventWithInfoForAccount> findAllByAccountIdAndState(Long accountId, String state, Pageable pageable) {
		return eventRepository.findAllByAccountIdAndState(accountId, State.fromString(state, null), pageable);
	}

	public Page<EventWithInfo> findAllByState(String state, Pageable pageable) {
		return eventRepository.findAllByState(State.fromString(state, null), pageable);
	}
}