package com.pareidolia.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cloud.openfeign.support.PageJacksonModule;
import org.springframework.cloud.openfeign.support.SortJacksonModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfiguration {
	@Bean
	public ObjectMapper mapper() {
		ObjectMapper mapperJson = new ObjectMapper();
		mapperJson.findAndRegisterModules();
		mapperJson.registerModule(new JavaTimeModule());
		mapperJson.registerModule(new PageJacksonModule());
		mapperJson.registerModule(new SortJacksonModule());
		return mapperJson;
	}
}
