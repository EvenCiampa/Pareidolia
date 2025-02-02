package com.pareidolia.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PromoterDTO extends AccountDTO implements Serializable {
	public String photo;
	public String presentation;

	@SuppressWarnings("unused")
	public PromoterDTO(Long id, String name, String surname, String phone, String email, String photo, String presentation, String referenceType, LocalDateTime creationTime) {
		super(id, name, surname, phone, email, referenceType, creationTime);
		this.photo = photo;
		this.presentation = presentation;
	}

	@SuppressWarnings("unused")
	public PromoterDTO(String photo, String presentation) {
		this.photo = photo;
		this.presentation = presentation;
	}
}
