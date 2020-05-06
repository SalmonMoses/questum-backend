package com.theteam.questerium.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {
	private final HandlerExceptionResolver handler;

	public ExceptionHandlerFilter(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handler) {
		this.handler = handler;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
	                                HttpServletResponse response,
	                                FilterChain filterChain) throws ServletException,
	                                                                IOException {
		try {
			filterChain.doFilter(request, response);
		} catch (Exception e) {
			handler.resolveException(request, response, null, e);
		}
	}
}
