package com.pareidolia.repository;

import com.pareidolia.entity.Account;
import com.pareidolia.entity.EventDraftPromoterAssociation;
import com.pareidolia.entity.PromoterInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventDraftPromoterAssociationRepository extends JpaRepository<EventDraftPromoterAssociation, Long> {
    // Trova l'associazione tra un evento e un promoter specifico
    Optional<EventDraftPromoterAssociation> findByIdEventDraftAndIdPromoter(Long idEventDraft, Long idPromoter);

    void deleteByIdEventDraftAndIdPromoter(Long eventDraftId, Long promoterId);

    @Query("""
            	SELECT org.springframework.data.util.Pair(a, pi)
            	FROM EventDraftPromoterAssociation edpa
            	INNER JOIN PromoterInfo pi ON pi.idPromoter = edpa.idPromoter
            	INNER JOIN Account a ON a.id = edpa.idPromoter
            	WHERE edpa.idEvent = :idEvent
            """)
    List<Pair<Account, PromoterInfo>> findPromotersByIdEventDraft(Long idEventDraft);
}
