package com.pareidolia.state;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class StateConverter implements AttributeConverter<State, String> {

	@Override
	public String convertToDatabaseColumn(State state) {
		return (state == null) ? null : state.getStateName();
	}

	@Override
	public State convertToEntityAttribute(String dbData) {
		// Qui NON possiamo ancora passare l'evento, quindi lasciamo un valore temporaneo
		return (dbData == null) ? null : State.fromString(dbData, null);
	}
}
