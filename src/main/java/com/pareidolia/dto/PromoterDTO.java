package com.pareidolia.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PromoterDTO implements Serializable {
	private Long id;
	private String name;
	private String surname;
	private String phone;
	private String email;
	private String photo;
	private String presentation;
}
