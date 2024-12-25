package com.pareidolia.repository;

import com.pareidolia.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

	@Query("""
			SELECT m
			FROM Message m
			INNER JOIN EventDraft ed ON  ed.id = m.idEventDraft
			WHERE m.idEventDraft = :idEventDraft
		""")
	Page<Message> findMessagesByIdEventDraft(Long idEventDraft, PageRequest pageRequest);
}