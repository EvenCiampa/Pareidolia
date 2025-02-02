package com.pareidolia.repository;

import com.pareidolia.entity.PromoterInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoterInfoRepository extends JpaRepository<PromoterInfo, Long> {
	@Query("""
			SELECT p
			FROM PromoterInfo p
			INNER JOIN EventPromoterAssociation ep ON ep.idPromoter = p.idPromoter
			WHERE ep.idEvent = :idEvent
		""")
	List<PromoterInfo> findAllByIdEvent(Long idEvent);

	@Query("""
			SELECT p
			FROM PromoterInfo p
			JOIN FETCH p.account
			WHERE p.account.email in :emails
		""")
	List<PromoterInfo> findAllByEmailIn(List<String> emails);

	Optional<PromoterInfo> findByIdPromoter(Long idPromoter);
}