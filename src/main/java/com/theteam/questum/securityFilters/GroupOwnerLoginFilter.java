package com.theteam.questum.securityFilters;

import com.theteam.questum.models.GroupOwner;
import com.theteam.questum.models.OwnerAuthToken;
import com.theteam.questum.repositories.GroupOwnersRepository;
import com.theteam.questum.repositories.OwnerAuthTokensRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(1)
public class GroupOwnerLoginFilter implements Filter {
	@Autowired
	final OwnerAuthTokensRepository tokens;

	public GroupOwnerLoginFilter(OwnerAuthTokensRepository tokens) {
		this.tokens = tokens;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_GROUP_OWNER"));
		Optional<OwnerAuthToken> token = tokens.findByToken(UUID.fromString(request.getParameter("authToken")));
		token.ifPresentOrElse(tok -> {
			GroupOwner owner = tok.getOwner();
			UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(owner, tok, authorities);
			auth.setAuthenticated(true);
		}, () -> {
			throw new BadCredentialsException("Can't find group owner with such credentials");
		});
		chain.doFilter(request, response);
	}
}
