package com.pareidolia.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class AccountLoginDTO extends AccountDTO implements Serializable {
	@ToString.Exclude
	public String authToken;
}
