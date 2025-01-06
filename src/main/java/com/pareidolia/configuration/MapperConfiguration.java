package com.pareidolia.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfiguration {
	@Bean
	public ObjectMapper mapper() {
		ObjectMapper mapperJson = new ObjectMapper();
		mapperJson.registerModule(new JavaTimeModule());
		return mapperJson;
	}
}
