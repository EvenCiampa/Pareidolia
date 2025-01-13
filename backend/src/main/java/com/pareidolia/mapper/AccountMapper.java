package com.pareidolia.mapper;


import com.pareidolia.dto.*;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.PromoterInfo;

public class AccountMapper {

	public static AccountDTO entityToAccountDTO(Account entity) {
		if (entity == null) return null;
		AccountDTO dto = new AccountDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setSurname(entity.getSurname());
		dto.setPhone(entity.getPhone());
		dto.setEmail(entity.getEmail());
		dto.setReferenceType(entity.getReferenceType().toString());
		return dto;
	}

	public static AdminDTO entityToAdminDTO(Account entity) {
		if (entity == null) return null;
		AdminDTO dto = new AdminDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setSurname(entity.getSurname());
		dto.setPhone(entity.getPhone());
		dto.setEmail(entity.getEmail());
		dto.setReferenceType(entity.getReferenceType().toString());
		return dto;
	}

	public static ConsumerDTO entityToConsumerDTO(Account entity) {
		if (entity == null) return null;
		ConsumerDTO dto = new ConsumerDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setSurname(entity.getSurname());
		dto.setPhone(entity.getPhone());
		dto.setEmail(entity.getEmail());
		dto.setReferenceType(entity.getReferenceType().toString());
		return dto;
	}

	public static PromoterDTO entityToPromoterDTO(Account entity, PromoterInfo promoterInfo) {
		if (entity == null) return null;
		PromoterDTO dto = new PromoterDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setSurname(entity.getSurname());
		dto.setPhone(entity.getPhone());
		dto.setEmail(entity.getEmail());
		dto.setReferenceType(entity.getReferenceType().toString());
		dto.setPhoto(promoterInfo.getPhoto());
		dto.setPresentation(promoterInfo.getPresentation());
		return dto;
	}

	public static Account registrationDTOToEntity(RegistrationDTO dto, Account.Type referenceType) {
		if (dto == null) return null;
		Account entity = new Account();
		entity.setEmail(dto.getEmail());
		entity.setPassword(dto.getPassword());
		entity.setName(dto.getName());
		entity.setSurname(dto.getSurname());
		entity.setPhone(dto.getPhone());
		entity.setReferenceType(referenceType);
		return entity;
	}

	public static void updateEntityWithAccountDTO(Account entity, AccountDTO dto) {
		entity.setEmail(dto.getEmail());
		entity.setName(dto.getName());
		entity.setSurname(dto.getSurname());
		entity.setPhone(dto.getPhone());
	}

	public static void updateEntityWithConsumerDTO(Account entity, ConsumerDTO dto) {
		entity.setEmail(dto.getEmail());
		entity.setName(dto.getName());
		entity.setSurname(dto.getSurname());
		entity.setPhone(dto.getPhone());
	}

	public static void updateEntitiesWithPromoterDTO(Account entity, PromoterInfo promoterInfo, PromoterDTO dto) {
		entity.setEmail(dto.getEmail());
		entity.setName(dto.getName());
		entity.setSurname(dto.getSurname());
		entity.setPhone(dto.getPhone());
		promoterInfo.setPresentation(dto.getPresentation());
	}

}
