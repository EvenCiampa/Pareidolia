package com.pareidolia.mapper;

import com.pareidolia.dto.MessageDTO;
import com.pareidolia.entity.Message;

import java.io.Serializable;

public class MessageMapper implements Serializable {
	public static MessageDTO entityToDTO(Message entity) {
		if (entity == null) return null;
		MessageDTO dto = new MessageDTO();
		dto.setId(entity.getId());
		dto.setMessage(entity.getMessage());
		dto.setIdAccount(entity.getIdAccount());
		dto.setAccountName(entity.getAccount().getName() + " " + entity.getAccount().getSurname());
		dto.setAccountReferenceType(entity.getAccount().getReferenceType().name());
		dto.setCreationTime(entity.getCreationTime());
		return dto;
	}
}