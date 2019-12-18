package com.theteam.questum.configuration;

//import com.theteam.questum.securityFilters.GroupOwnerLoginFilter;

import com.theteam.questum.security.QuestumAuthManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
	QuestumAuthManager authManager;

	@Autowired
	public ResourceServerConfiguration(QuestumAuthManager authManager) {
		this.authManager = authManager;
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
		    .antMatchers("/login/**", "/check/**", "/signup/**")
		    .permitAll()
		    .and()
		    .authorizeRequests().anyRequest().authenticated();
	}

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.authenticationManager(authManager);
	}
}
