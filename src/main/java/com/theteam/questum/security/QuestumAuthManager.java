package com.theteam.questum.security;

import com.theteam.questum.exceptions.TokenExpiredException;
import com.theteam.questum.models.AuthToken;
import com.theteam.questum.repositories.TokenRepository;
import com.theteam.questum.services.QuestumAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Optional;

@Component
public class QuestumAuthManager implements AuthenticationManager {
	@Autowired
	TokenRepository tokens;

	@Autowired
	QuestumAuthService authService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String tokenString = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
		Optional<AuthToken> token = tokens.findByToken(tokenString);
		if (token.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
		}
		if(token.get().getExpirationDate().getTime() < System.currentTimeMillis()) {
			throw new TokenExpiredException(token.get().getToken());
		}
		switch (token.get().getType()) {
			case "OWNER": {
				GroupOwnerPrincipal details = authService.handleOwnerLogin(token.get());
				ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
				authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
				return new UsernamePasswordAuthenticationToken(details, token.get(),
				                                               authorities);
			}
			case "USER": {
				ParticipantPrincipal details = authService.handleUserLogin(token.get());
				ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
				authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
				return new UsernamePasswordAuthenticationToken(details, token.get(),
				                                               authorities);
			}
			default: {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
			}
		}
	}
}
