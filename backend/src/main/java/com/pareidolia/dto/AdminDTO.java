package com.pareidolia.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class AdminDTO extends AccountDTO implements Serializable {
	@SuppressWarnings("unused")
	public AdminDTO(Long id, String name, String surname, String phone, String email, String referenceType) {
		super(id, name, surname, phone, email, referenceType);
	}
}
