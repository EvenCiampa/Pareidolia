package com.pareidolia.repository;

import com.pareidolia.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	List<Message> findByIdEventOrderByCreationTimeDesc(Long idEvent);

	Page<Message> findByIdEvent(Long idEvent, Pageable pageable);
}