package com.theteam.questerium.security;

import com.theteam.questerium.repositories.TokenRepository;
import com.theteam.questerium.services.JwtService;
import com.theteam.questerium.services.QuestumAuthService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
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

@Component
public class QuestumAuthManager implements AuthenticationManager {
	@Autowired
	TokenRepository tokens;

	@Autowired
	QuestumAuthService authService;

	@Autowired
	private JwtService jwtService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String tokenString = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
		Jws<Claims> jwsToken = jwtService.parseAccessToken(tokenString);
//		if(jwsToken.getBody().getExpiration().before(new Date())) {
//			throw new TokenExpiredException(token.get().getToken());
//		}
		switch (jwsToken.getBody().get("rol", String.class)) {
			case "owner": {
				GroupOwnerPrincipal details = authService.handleOwnerLogin(jwsToken);
				ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
				authorities.add(new SimpleGrantedAuthority("ROLE_OWNER"));
				return new UsernamePasswordAuthenticationToken(details, jwsToken,
				                                               authorities);
			}
			case "participant": {
				ParticipantPrincipal details = authService.handleUserLogin(jwsToken);
				ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<>();
				authorities.add(new SimpleGrantedAuthority("ROLE_PARTICIPANT"));
				return new UsernamePasswordAuthenticationToken(details, jwsToken,
				                                               authorities);
			}
			default: {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
			}
		}
	}
}
