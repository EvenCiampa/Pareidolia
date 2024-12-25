package com.pareidolia.repository;


import com.pareidolia.entity.EventDraft;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface EventDraftRepository extends JpaRepository<EventDraft, Long> {
	@Query("""
		SELECT ed
		FROM EventDraft ed
		INNER JOIN EventDraftPromoterAssociation edpa ON edpa.idEventDraft = ed.id
		WHERE edpa.idPromoter = :idPromoter
	""")
	Page<EventDraft> findAllByPromoterId(Long idPromoter, PageRequest pageRequest);
}