package com.pareidolia.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AdminDTO extends AccountDTO implements Serializable {
	@SuppressWarnings("unused")
	public AdminDTO(Long id, String name, String surname, String phone, String email, String referenceType, LocalDateTime creationTime) {
		super(id, name, surname, phone, email, referenceType, creationTime);
	}
}
