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
		dto.setCreationTime(entity.getCreationTime());
		return dto;
	}

	public static Message dtoToEntity(MessageDTO dto) {
		if (dto == null) return null;
		Message entity = new Message();
		entity.setId(dto.getId());
		entity.setMessage(dto.getMessage());
		entity.setIdAccount(dto.getIdAccount());
		entity.setCreationTime(dto.getCreationTime());
		return entity;
	}
}