package com.theteam.questerium.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application-dev.yaml")
@PropertySource("classpath:application-prod.yaml")
@PropertySource("classpath:application-test.yaml")
public class EmailConfiguration {
	@Value("sendgrid.api")
	private String apiKey;
}
