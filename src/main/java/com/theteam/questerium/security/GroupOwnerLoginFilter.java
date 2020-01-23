//package com.theteam.questum.securityFilters;
//
//import com.theteam.questum.models.GroupOwner;
//import com.theteam.questum.models.OwnerAuthToken;
//import com.theteam.questum.repositories.OwnerAuthTokensRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.annotation.Order;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.*;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@Component
//@Order(1)
//public class GroupOwnerLoginFilter implements Filter {
//	private final OwnerAuthTokensRepository tokens;
//
//	@Autowired
//	public GroupOwnerLoginFilter(OwnerAuthTokensRepository tokens) {
//		this.tokens = tokens;
//	}
//
//	@Override
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
//			ServletException {
//		HttpServletRequest req = (HttpServletRequest) request;
//		HttpServletResponse res = (HttpServletResponse) response;
//		List<GrantedAuthority> authorities = new ArrayList<>();
//		authorities.add(new SimpleGrantedAuthority("ROLE_GROUP_OWNER"));
//		String bearerTokenHeader = req.getHeader(HttpHeaders.AUTHORIZATION);
//		if (bearerTokenHeader == null) {
//			res.setStatus(HttpStatus.UNAUTHORIZED.value());
//			return;
//		}
//		String[] bearerToken = bearerTokenHeader.split(" ");
//		if (!bearerToken[0].equals("Bearer")) {
//			throw new BadCredentialsException("Invalid token!");
//		}
//		Optional<OwnerAuthToken> token =
//				tokens.findByToken(bearerToken[1]);
//		token.ifPresentOrElse(
//				tok -> {
//					GroupOwner owner = tok.getOwner();
//					UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(owner, tok,
//					                                                                                   authorities);
//					SecurityContextHolder.getContext().setAuthentication(auth);
////					auth.setAuthenticated(true);
//				},
//				() -> {
//					res.setStatus(HttpStatus.UNAUTHORIZED.value());
//				}
//		);
//		chain.doFilter(request, response);
//	}
//}
