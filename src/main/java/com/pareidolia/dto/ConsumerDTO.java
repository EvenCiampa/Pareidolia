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
public class ConsumerDTO extends AccountDTO implements Serializable {
	@SuppressWarnings("unused")
	public ConsumerDTO(Long id, String name, String surname, String phone, String email, String referenceType) {
		super(id, name, surname, phone, email, referenceType);
	}
}
