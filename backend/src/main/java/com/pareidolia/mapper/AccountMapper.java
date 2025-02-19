package com.pareidolia.mapper;

import com.pareidolia.dto.*;
import com.pareidolia.entity.Account;
import com.pareidolia.entity.PromoterInfo;
import org.apache.commons.codec.digest.DigestUtils;

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
		dto.setCreationTime(entity.getCreationTime());
		return dto;
	}

	public static AccountLoginDTO entityToAccountLoginDTO(Account entity, String authToken) {
		if (entity == null) return null;
		AccountLoginDTO dto = new AccountLoginDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setSurname(entity.getSurname());
		dto.setPhone(entity.getPhone());
		dto.setEmail(entity.getEmail());
		dto.setReferenceType(entity.getReferenceType().toString());
		dto.setAuthToken(authToken);
		dto.setCreationTime(entity.getCreationTime());
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
		dto.setCreationTime(entity.getCreationTime());
		return dto;
	}

	public static ReviewerDTO entityToReviewerDTO(Account entity) {
		if (entity == null) return null;
		ReviewerDTO dto = new ReviewerDTO();
		dto.setId(entity.getId());
		dto.setName(entity.getName());
		dto.setSurname(entity.getSurname());
		dto.setPhone(entity.getPhone());
		dto.setEmail(entity.getEmail());
		dto.setReferenceType(entity.getReferenceType().toString());
		dto.setCreationTime(entity.getCreationTime());
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
		dto.setCreationTime(entity.getCreationTime());
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
		dto.setCreationTime(entity.getCreationTime());
		return dto;
	}

	public static Account registrationDTOToEntity(RegistrationDTO dto, Account.Type referenceType) {
		if (dto == null) return null;
		Account entity = new Account();
		entity.setEmail(dto.getEmail());
		entity.setPassword(DigestUtils.sha3_256Hex(dto.getPassword()));
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

	public static void updateEntityWithReviewerDTO(Account entity, ReviewerDTO dto) {
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
