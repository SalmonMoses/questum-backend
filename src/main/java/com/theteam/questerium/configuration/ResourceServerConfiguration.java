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
import org.springframework.security.web.authentication.logout.LogoutFilter;

@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {
	QuestumAuthManager authManager;
	ExceptionHandlerFilter exceptionsHandlerFilter;

	@Autowired
	public ResourceServerConfiguration(QuestumAuthManager authManager, ExceptionHandlerFilter exceptionHandlerFilter) {
		this.authManager = authManager;
		this.exceptionsHandlerFilter = exceptionHandlerFilter;
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.addFilterBefore( exceptionsHandlerFilter, LogoutFilter.class );
		http.cors().and().csrf().disable()
		    .authorizeRequests()
		    .antMatchers("/login/**", "/signup/**", "/restore-pswd/**", "/check/group", "/check/participant")
		    .permitAll()
		    .and()
		    .authorizeRequests().anyRequest().authenticated();
	}

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.authenticationManager(authManager);
	}
}
