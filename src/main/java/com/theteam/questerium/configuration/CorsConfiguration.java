package com.theteam.questerium.configuration;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CorsConfiguration implements WebMvcConfigurer {
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE");
//		        .allowedOrigins("http://localhost:3000",
//		                        "https://questerium.herokuapp.com",
//		                        "http://questerium.herokuapp.com");
	}
}
