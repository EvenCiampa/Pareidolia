package com.pareidolia.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO implements Serializable {
	public Long id;
	public String name;
	public String surname;
	public String phone;
	public String email;
	public String referenceType;
}
