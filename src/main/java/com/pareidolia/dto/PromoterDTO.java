package com.pareidolia.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString(callSuper = true)
@NoArgsConstructor
public class PromoterDTO extends AccountDTO implements Serializable {
	public String photo;
	public String presentation;

	@SuppressWarnings("unused")
	public PromoterDTO(Long id, String name, String surname, String phone, String email, String photo, String presentation, String referenceType) {
		super(id, name, surname, phone, email, referenceType);
		this.photo = photo;
		this.presentation = presentation;
	}

	@SuppressWarnings("unused")
	public PromoterDTO(String photo, String presentation) {
		this.photo = photo;
		this.presentation = presentation;
	}
}
