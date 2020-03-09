package com.theteam.questerium.configuration;

//import com.theteam.questum.securityFilters.GroupOwnerLoginFilter;

import com.theteam.questerium.security.QuestumAuthManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter implements WebMvcConfigurer {
	QuestumAuthManager authManager;

	@Autowired
	public ResourceServerConfiguration(QuestumAuthManager authManager) {
		this.authManager = authManager;
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.cors().and().csrf().disable()
		    .authorizeRequests()
		    .antMatchers("/login/**", "/signup/**", "/check/**")
		    .permitAll()
		    .and()
		    .authorizeRequests().anyRequest().authenticated();
	}

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.authenticationManager(authManager);
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE");
//		        .allowedOrigins("http://localhost:3000",
//		                        "https://questerium.herokuapp.com",
//		                        "http://questerium.herokuapp.com");
	}
}
