package com.pareidolia.repository;

import com.pareidolia.entity.Account;
import com.pareidolia.entity.EventPromoterAssociation;
import com.pareidolia.entity.PromoterInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventPromoterAssociationRepository extends JpaRepository<EventPromoterAssociation, Long> {

	@Query("""
			SELECT new org.springframework.data.util.Pair(a, pi)
			FROM EventPromoterAssociation epa
			INNER JOIN PromoterInfo pi ON pi.idPromoter = epa.idPromoter
			INNER JOIN Account a ON a.id = epa.idPromoter
			WHERE epa.idEvent = :idEvent
		""")
	List<Pair<Account, PromoterInfo>> findPromotersByIdEvent(Long idEvent);

	Optional<EventPromoterAssociation> findByIdEventAndIdPromoter(Long idEvent, Long idPromoter);

	void deleteByIdEventAndIdPromoter(Long eventId, Long promoterId);
}