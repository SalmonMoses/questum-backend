package com.theteam.questum.security;

import com.theteam.questum.models.AuthToken;
import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.models.QuestGroup;
import com.theteam.questum.repositories.TokenRepository;
import com.theteam.questum.services.QuestumAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
			return null;
		}
		switch (token.get().getType()) {
			case "ADMIN": {
				GroupOwnerDetails details = authService.handleOwnerLogin(token.get());
				ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
				authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
				return new UsernamePasswordAuthenticationToken(details, token.get(),
				                                               authorities);
			}
			case "USER": {
				return null;
			}
			case "CLIENT": {
				return null;
			}
			default: {
				return null;
			}
		}
	}
}
